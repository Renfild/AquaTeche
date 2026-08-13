package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AquaTechUI.MOD_ID);

    public static final RegistryObject<SoundEvent> PULL_TICK = register("minigame.pull_tick");
    public static final RegistryObject<SoundEvent> SAFE_CHIME = register("minigame.safe_chime");
    public static final RegistryObject<SoundEvent> DANGER_WARN = register("minigame.danger_warn");
    public static final RegistryObject<SoundEvent> CATCH_SUCCESS = register("minigame.catch_success");
    public static final RegistryObject<SoundEvent> LINE_SNAP = register("minigame.line_snap");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(AquaTechUI.MOD_ID, id)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
