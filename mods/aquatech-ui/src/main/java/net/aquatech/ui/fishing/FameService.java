package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Стена славы: рекорд сервера по весу для каждого вида рыбы (config/aquatech_hall_of_fame.json).
 * Рыба, поставившая рекорд, становится именным предметом: «Лещ «Ник»» с весом и датой в описании.
 * Работает только на сервере, в основном потоке.
 */
public final class FameService {

    /** Тег именной рыбы на предмете: владелец, вес в граммах и дата поимки. */
    public static final String RECORD_TAG = "AquaRecord";

    public record Change(String species, FameLogic.Outcome outcome, FameLogic.Record record, FameLogic.Record previous) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type RECORDS_TYPE = new TypeToken<LinkedHashMap<String, FameLogic.Record>>() {
    }.getType();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aquatech_hall_of_fame.json");
    private static final List<Consumer<Change>> LISTENERS = new CopyOnWriteArrayList<>();

    private static Map<String, FameLogic.Record> records;

    private FameService() {
    }

    public static void addListener(Consumer<Change> listener) {
        LISTENERS.add(listener);
    }

    /** Вид с рекордом на заданном месте (1 = самая тяжёлая рыба сервера), пустая строка, если столько рекордов нет. */
    public static String speciesAtRank(int rank) {
        List<Map.Entry<String, FameLogic.Record>> ranked = new ArrayList<>(records().entrySet());
        ranked.sort((a, b) -> Integer.compare(b.getValue().grams, a.getValue().grams));
        return rank >= 1 && rank <= ranked.size() ? ranked.get(rank - 1).getKey() : "";
    }

    /** Рекорд вида или null, если места на стене пока пустое. */
    public static FameLogic.Record get(String species) {
        return records().get(species);
    }

    /**
     * Проверяет улов на рекорд до того, как рыба попала в инвентарь, и штампует именную рыбу прямо в {@code drops}.
     * Если в стопке больше одной рыбы, рекордная отделяется в свой предмет.
     */
    public static void onCatch(ServerPlayer player, List<ItemStack> drops) {
        if (drops == null || drops.isEmpty()) {
            return;
        }
        List<ItemStack> split = new ArrayList<>();
        for (ItemStack stack : new ArrayList<>(drops)) {
            FishCatchInfo.Info info = FishCatchInfo.read(stack);
            if (info == null) {
                continue;
            }
            String species = FishCatchInfo.speciesId(stack);
            FishWeight.Species data = FishWeight.species(species);
            if (data == null) {
                continue;
            }
            String fish = FishCatchInfo.displayName(stack);
            FameLogic.Record candidate = new FameLogic.Record(player.getUUID().toString(),
                    player.getGameProfile().getName(), fish, info.grams(), info.cm(), LocalDate.now().toString());
            FameLogic.Record[] previous = new FameLogic.Record[1];
            FameLogic.Outcome outcome = FameLogic.apply(records(), species, candidate, data.avgG(), previous);
            if (outcome == FameLogic.Outcome.NONE) {
                continue;
            }
            ItemStack trophy = stack;
            if (stack.getCount() > 1) {
                trophy = stack.split(1);
                split.add(trophy);
            }
            stamp(trophy, candidate);
            save();
            announce(player, outcome, candidate, previous[0]);
            Change change = new Change(species, outcome, candidate, previous[0]);
            for (Consumer<Change> listener : LISTENERS) {
                listener.accept(change);
            }
        }
        drops.addAll(split);
    }

    private static void stamp(ItemStack trophy, FameLogic.Record record) {
        // без «§»: Starcatcher пропускает имя рыбы с весом через свою разметку и съедает такие коды;
        // цвет по редкости он добавит сам
        trophy.setHoverName(Component.literal(record.fish + " «" + record.name + "»")
                .withStyle(s -> s.withItalic(false)));
        CompoundTag tag = new CompoundTag();
        tag.putString("owner", record.name);
        tag.putString("uuid", record.uuid);
        tag.putInt("grams", record.grams);
        tag.putString("date", record.date);
        trophy.getOrCreateTag().put(RECORD_TAG, tag);
        ListTag lore = new ListTag();
        lore.add(loreLine("§6★ Рекорд сервера на момент поимки"));
        lore.add(loreLine("§7Вес: §f" + FishWeight.format(record.grams) + "§7, длина: §f" + record.cm + " см"));
        lore.add(loreLine("§7Поймал §f" + record.name + "§7, " + record.date));
        trophy.getOrCreateTagElement("display").put("Lore", lore);
    }

    private static StringTag loreLine(String text) {
        return StringTag.valueOf(Component.Serializer.toJson(
                Component.literal(text).withStyle(s -> s.withItalic(false))));
    }

    private static void announce(ServerPlayer player, FameLogic.Outcome outcome, FameLogic.Record record,
                                 FameLogic.Record previous) {
        String weight = FishWeight.format(record.grams);
        String message = switch (outcome) {
            case FIRST -> "§6[Стена славы] §f" + record.name + " занял пустое место: §b" + record.fish
                    + " §7(" + weight + "). §fПобейте!";
            case TAKEN -> "§6[Стена славы] §f" + record.name + " отобрал рекорд у §c" + (previous == null ? "?" : previous.name)
                    + "§f: §b" + record.fish + " §7— " + weight + (previous == null ? "" : " (было " + FishWeight.format(previous.grams) + ")");
            case OWN -> "§6[Стена славы] §f" + record.name + " побил свой рекорд: §b" + record.fish + " §7— " + weight;
            case NONE -> "";
        };
        if (player.getServer() != null && !message.isEmpty()) {
            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static Map<String, FameLogic.Record> records() {
        if (records == null) {
            Map<String, FameLogic.Record> loaded = null;
            try {
                if (Files.exists(FILE)) {
                    loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), RECORDS_TYPE);
                }
            } catch (IOException | RuntimeException e) {
                AquaTechUI.LOGGER.warn("[fame] не удалось прочитать {}: {}", FILE.getFileName(), e.toString());
                backupBroken();
            }
            records = loaded != null ? loaded : new LinkedHashMap<>();
        }
        return records;
    }

    /** Битый файл не затираем молча: сохраняем рядом копию, прежде чем начать с пустой стены. */
    private static void backupBroken() {
        try {
            Files.copy(FILE, FILE.resolveSibling(FILE.getFileName() + ".broken"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(records(), RECORDS_TYPE), StandardCharsets.UTF_8);
        } catch (IOException e) {
            AquaTechUI.LOGGER.warn("[fame] не удалось записать {}: {}", FILE.getFileName(), e.toString());
        }
    }
}
