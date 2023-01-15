package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class KeyMappings {

    //TODO: make custom key category
    public static final KeyMapping SPELL_WHEEL_KEYMAP = registerKey(InputConstants.KEY_R, "spell_wheel", KeyMapping.CATEGORY_INTERFACE);
    public static final KeyMapping SPELLBAR_SCROLL_MODIFIER_KEYMAP = registerKey(InputConstants.KEY_LSHIFT, "spellbar_scroll_modifier", KeyMapping.CATEGORY_INTERFACE);

    private static KeyMapping registerKey(int keycode, String name, String category) {
        return new KeyMapping("key.testmod." + name, keycode, category);
    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
        TestMod.LOGGER.debug("KeyMappings.onRegisterKeybinds");
        event.register(SPELL_WHEEL_KEYMAP);
        event.register(SPELLBAR_SCROLL_MODIFIER_KEYMAP);
    }
}
