package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.gui.SpellWheelDisplay;
import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
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

    private static final KeyState SPELL_WHEEL_STATE = register(KeyMappings.SPELL_WHEEL_KEYMAP);
    private static final KeyState TEST_STATE = register(KeyMappings.TEST_KEYMAP);

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        var minecraft = Minecraft.getInstance();

        //
        // "consumeClick()"
        //

        Player player = minecraft.player;
        if (SPELL_WHEEL_STATE.wasPressed()) {
            TestMod.LOGGER.info("Keypress: {}", SPELL_WHEEL_STATE.key.getKey());
            if (minecraft.screen == null)
                SpellWheelDisplay.open();
        }
        if (SPELL_WHEEL_STATE.wasReleased()) {
            if (minecraft.screen == null)
                SpellWheelDisplay.close();

            TestMod.LOGGER.info("Key released: {}", SPELL_WHEEL_STATE.key.getKey());
        }


        Update();
    }

    private static void Update() {
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
