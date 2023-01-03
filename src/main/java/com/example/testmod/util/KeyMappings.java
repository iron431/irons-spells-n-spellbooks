package com.example.testmod.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public final class KeyMappings {

    public static final KeyMapping testKey = registerKey(InputConstants.KEY_R,"test",KeyMapping.CATEGORY_INTERFACE);

    private static KeyMapping registerKey( int keycode, String name, String category) {
        final var key = new KeyMapping("key.testmod."+name,keycode,category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
