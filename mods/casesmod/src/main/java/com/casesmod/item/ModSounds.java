package com.casesmod.item;

import com.casesmod.CasesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Собственные звуки мода для открытия кейсов — синтезированы отдельно, не позаимствованы из ванильных. */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CasesMod.MOD_ID);

    public static final RegistryObject<SoundEvent> CASE_TICK = register("case_tick");
    public static final RegistryObject<SoundEvent> ANTICIPATION_RISER = register("anticipation_riser");
    public static final RegistryObject<SoundEvent> REVEAL_COMMON = register("reveal_common");
    public static final RegistryObject<SoundEvent> REVEAL_UNCOMMON = register("reveal_uncommon");
    public static final RegistryObject<SoundEvent> REVEAL_RARE = register("reveal_rare");
    public static final RegistryObject<SoundEvent> REVEAL_EPIC = register("reveal_epic");
    public static final RegistryObject<SoundEvent> REVEAL_LEGENDARY = register("reveal_legendary");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(CasesMod.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
