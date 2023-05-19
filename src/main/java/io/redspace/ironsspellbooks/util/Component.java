package io.redspace.ironsspellbooks.util;


import net.minecraft.network.chat.*;

public class Component {

    public static net.minecraft.network.chat.TranslatableComponent translatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    public static net.minecraft.network.chat.TextComponent literal(String key) {
        return new net.minecraft.network.chat.TextComponent(key);
    }

    public static net.minecraft.network.chat.Component empty() {
        return TextComponent.EMPTY;
    }
}
