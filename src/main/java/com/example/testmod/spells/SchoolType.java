package com.example.testmod.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;

public enum SchoolType {
    FIRE(0),
    ICE(1),
    LIGHTNING(2),
    HOLY(3),
    ENDER(4),
    BLOOD(5),
    EVOCATION(6);

    private final int value;

    SchoolType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public Component getDisplayName() {
        return DISPLAYS[getValue()];
    }

    public static final Component DISPLAY_FIRE = new TranslatableComponent("school.testmod.fire").withStyle(ChatFormatting.GOLD);
    public static final Component DISPLAY_ICE = new TranslatableComponent("school.testmod.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff));
    public static final Component DISPLAY_LIGHTNING = new TranslatableComponent("school.testmod.lightning").withStyle(ChatFormatting.AQUA);
    public static final Component DISPLAY_HOLY = new TranslatableComponent("school.testmod.holy").withStyle(Style.EMPTY.withColor(0xfff8d4));
    public static final Component DISPLAY_ENDER = new TranslatableComponent("school.testmod.ender").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final Component DISPLAY_BLOOD = new TranslatableComponent("school.testmod.blood").withStyle(ChatFormatting.DARK_RED);
    public static final Component DISPLAY_EVOCATION = new TranslatableComponent("school.testmod.evocation").withStyle(ChatFormatting.WHITE);
    public static final Component[] DISPLAYS = {DISPLAY_FIRE, DISPLAY_ICE, DISPLAY_LIGHTNING, DISPLAY_HOLY, DISPLAY_ENDER, DISPLAY_BLOOD, DISPLAY_EVOCATION};


}
