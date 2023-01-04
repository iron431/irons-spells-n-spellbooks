package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientKeyHandler {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();
    private static final KeyState SPELL_WHEEL_STATE = register(KeyMappings.spellWheel);
    private static final KeyState TEST_STATE = register(KeyMappings.test);

//    public static void register() {
//        spellWheelState = ;
//        testState = KeyStates.put(KeyMappings.test, new KeyState(KeyMappings.test));
//    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (SPELL_WHEEL_STATE.wasPressed())
            TestMod.LOGGER.info("R");
        if (TEST_STATE.wasPressed())
            TestMod.LOGGER.info("G");

        for (KeyState k : KEY_STATES) {
            k.Update();
        }

    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }

}
