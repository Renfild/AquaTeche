package com.casesmod.item;

import com.casesmod.CasesMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Регистрация собственных предметов мода. Ключи от кейсов больше не используются — открытие идёт за валюту через GUI. */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CasesMod.MOD_ID);

    public static final RegistryObject<Item> MENU_OPENER = ITEMS.register("menu_opener",
            () -> new MenuOpenerItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
