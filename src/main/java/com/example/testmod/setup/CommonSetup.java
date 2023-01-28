package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.config.CommonConfigs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    @SubscribeEvent()
    public static void onModConfigEvent(ModConfigEvent event) {
        CommonConfigs.resolveQueue();
    }
}
