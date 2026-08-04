package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class OceanGuideBookScreen extends Screen {

    private int currentPage = 0;
    private final List<GuidePage> pages = new ArrayList<>();

    public static class GuidePage {
        public final String chapterTitle;
        public final String title;
        public final List<String> lines;

        public GuidePage(String chapterTitle, String title, List<String> lines) {
            this.chapterTitle = chapterTitle;
            this.title = title;
            this.lines = lines;
        }
    }

    public OceanGuideBookScreen() {
        super(Component.literal("Энциклопедия Океана"));
        initPages();
    }

    private void initPages() {
        pages.clear();

        pages.add(new GuidePage("ВВЕДЕНИЕ", "Мир AquaTech", List.of(
                "Земля затоплена. Руды — в глубинах.",
                "",
                "Путь выживания:",
                "• Ресурсовые удочки вместо шахт",
                "• Созвездия Океана — клавиша K",
                "• Маршрут Горизонта — статус флота",
                "• Машины: рыболов, фильтр, драгер",
                "• Давление растёт с глубиной — берегись"
        )));

        pages.add(new GuidePage("ГОРИЗОНТ", "Маршрут флота", List.of(
                "Параллельная дорога к сюжету FTB:",
                "H0 Пролог → H1 Матрос → H2 Шкипер",
                "→ H3 Капитан → H4 Адмирал → H5 Легенда",
                "",
                "/aquatech horizon — твой ранг",
                "/aquatech daily — контракт дня",
                "/aquatech season — уровень сезона",
                "",
                "Варпы: pier · market · atoll · harbor"
        )));

        pages.add(new GuidePage("СЕССИЯ", "Куда идти 15–45 мин", List.of(
                "1. Смотри HUD: контракт дня N/M",
                "2. /aquatech daily — условие и сдача",
                "3. FTB → «Маршрут Горизонта»",
                "4. /warp market — продай улов",
                "5. K — один навык Созвездий",
                "",
                "Daily даёт Aqua XP и XP сезона."
        )));

        pages.add(new GuidePage("УДОЧКИ", "11 тиров удочек", List.of(
                "Каждая удочка ловит свои ресурсы:",
                "• Новичок — дерево, камень, водоросли",
                "• Железо / Золото — руды и слитки",
                "• Алмаз / Изумруд — драгоценности",
                "• Незерит / Призмарин — элитные тиры",
                "• Абиссальная — осколки эха и реликвии",
                "",
                "Shift+ПКМ по удочке — снасти и приманки"
        )));

        pages.add(new GuidePage("СНАСТИ", "Снасти и приманки", List.of(
                "Снасть Удачи — шанс редкого бонуса",
                "Снасть Изобилия — больше предметов",
                "Снасть Скорости — поклёвка быстрее",
                "Абиссальная — доступ к глубокому луту",
                "",
                "Приманки добавляют тематические дропы:",
                "магнитная, кинетическая, термальная,",
                "абиссальная."
        )));

        pages.add(new GuidePage("СОЗВЕЗДИЯ", "Древо навыков (K)", List.of(
                "55 навыков в пяти ветках:",
                "• Рыболов — скорость и улов",
                "• Техника — машины и FE",
                "• Удача — редкий лут",
                "• Дайвинг — дыхание и плавание",
                "• Биология — водоросли и реген",
                "",
                "Стоимость: 1 / 2 / 3 очка по типу узла.",
                "Очки даются при повышении уровня XP."
        )));

        pages.add(new GuidePage("ГЛУБИНЫ", "Давление и глубина", List.of(
                "Уровень моря — Y=190. Глубина в HUD",
                "= 190 − ваша координата Y.",
                "",
                "Давление = глубина − запас защиты.",
                "Пока в HUD «норма» или «лёгкое»",
                "— сжатие не наносит урон.",
                "",
                "Среднее — усталость, высокое",
                "и критичное — урон и течения.",
                "",
                "Строить нельзя глубже 50 м",
                "от моря (ниже Y=140)."
        )));

        pages.add(new GuidePage("ГЛУБИНЫ", "Броня и дайвинг", List.of(
                "Броня даёт запас глубины:",
                "• +2 м за каждое очко брони",
                "• +4 м за прочность брони",
                "• +10 м базовый запас",
                "",
                "Железный ≈ 40 м, Алмаз/Незерит — глубже",
                "",
                "«Давление Бездны» и «Стойкость» — +6 м.",
                "Сонар-очки — +4 м."
        )));

        pages.add(new GuidePage("СОНАР", "Очки и импульс", List.of(
                "Сонар-очки (шлем):",
                "• Ночное зрение и сила кондуита",
                "• Присед под водой — сонар-импульс",
                "  подсвечивает существ рядом (КД 8с)",
                "",
                "На большой глубине за вами тянется",
                "биолюминесцентный след — ориентир",
                "в тёмной воде."
        )));

        pages.add(new GuidePage("ЭНЕРГЕТИКА", "Гидро-реактор", List.of(
                "Главный источник FE сборки.",
                "Базовая выработка ~1200 FE/t.",
                "",
                "Топливо: Концентрированный Био-Пеллет.",
                "Модуль эффективности повышает выход.",
                "Навыки Техники усиливают реактор.",
                "",
                "Питает Auto-Fisher, фильтр, драгер",
                "и совместимые машины других модов."
        )));

        pages.add(new GuidePage("АЛХИМИЯ", "Алтарь реликвий", List.of(
                "Синтез Тризубца Нептуна:",
                "1. Осколок Эха",
                "2. Сердце Моря",
                "3. Кристалл Призмарина",
                "4. Звезда Незера",
                "",
                "Тризубец: молния по взгляду,",
                "под водой — ударная волна.",
                "Портал Бездны: баффы 90 сек (КД 3 мин)."
        )));

        pages.add(new GuidePage("ПРИЛИВ", "Луна и шторм", List.of(
                "Рыбалка меняется с условиями:",
                "",
                "• Полнолуние — шанс сокровища↑",
                "• Новолуние — больше количества",
                "• Шторм Горизонта — редкий лут ×2",
                "  (навыки удачи и снасти)",
                "",
                "Абиссальный магнит притягивает",
                "предметы в радиусе 8 блоков."
        )));
    }

    @Override
    protected void init() {
        super.init();

        int pX = (this.width - 240) / 2;
        int pY = (this.height - 180) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            if (currentPage > 0) {
                currentPage--;
                Minecraft.getInstance().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
        }).bounds(pX + 10, pY + 155, 30, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            if (currentPage < pages.size() - 1) {
                currentPage++;
                Minecraft.getInstance().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
        }).bounds(pX + 200, pY + 155, 30, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int pX = (this.width - 240) / 2;
        int pY = (this.height - 180) / 2;

        g.fill(pX, pY, pX + 240, pY + 180, 0xFFF5E6C8);
        g.fill(pX, pY, pX + 240, pY + 2, 0xFF0284C7);
        UiDraw.border(g, pX, pY, 240, 180, 0xFF78350F);

        GuidePage page = pages.get(currentPage);

        g.fill(pX + 10, pY + 10, pX + 230, pY + 24, 0xFF0F172A);
        drawCenteredNoShadow(g, "≋ " + page.chapterTitle + " ≋", pX + 120, pY + 13, UiDraw.COLOR_PRIMARY);

        g.drawString(this.font, page.title, pX + 15, pY + 32, 0xFF78350F, false);
        g.fill(pX + 15, pY + 43, pX + 225, pY + 44, 0xFFB45309);

        int lineY = pY + 50;
        for (String line : page.lines) {
            g.drawString(this.font, line, pX + 15, lineY, 0xFF1E293B, false);
            lineY += 11;
        }

        drawCenteredNoShadow(g, (currentPage + 1) + " / " + pages.size(), pX + 120, pY + 160, 0xFF78350F);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawCenteredNoShadow(GuiGraphics g, String text, int centerX, int y, int color) {
        g.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
