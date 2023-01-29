package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.config.ServerConfigs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent()
    public static void onModConfigLoadingEvent(ModConfigEvent.Loading event) {
        TestMod.LOGGER.debug("onModConfigLoadingEvent");
        ServerConfigs.cacheConfigs();
        //SpellRarity.rarityTest();
    }

    @SubscribeEvent()
    public static void onModConfigReloadingEvent(ModConfigEvent.Reloading event) {
        TestMod.LOGGER.debug("onModConfigReloadingEvent");
    }
}
