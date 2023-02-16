package com.example.testmod.spells;

import com.example.testmod.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

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

    public static SchoolType getSchoolFromItem(ItemStack stack) {
        if (stack.is(ModTags.FIRE_FOCUS)) {
            return FIRE;
        } else if (stack.is(ModTags.ICE_FOCUS)) {
            return ICE;
        } else if (stack.is(ModTags.LIGHTNING_FOCUS)) {
            return LIGHTNING;
        } else if (stack.is(ModTags.HOLY_FOCUS)) {
            return HOLY;
        } else if (stack.is(ModTags.ENDER_FOCUS)) {
            return ENDER;
        } else if (stack.is(ModTags.BLOOD_FOCUS)) {
            return BLOOD;
            //TODO: evocation gem?
        } else if (stack.is(ModTags.EVOCATION_FOCUS)) {
            return EVOCATION;
        }/*else if (Items.ECHO_SHARD.equals(item)) {
            return VOID;
        }*/ else return null;
    }

    public static final Component DISPLAY_FIRE = Component.translatable("school.testmod.fire").withStyle(ChatFormatting.GOLD);
    public static final Component DISPLAY_ICE = Component.translatable("school.testmod.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff));
    public static final Component DISPLAY_LIGHTNING = Component.translatable("school.testmod.lightning").withStyle(ChatFormatting.AQUA);
    public static final Component DISPLAY_HOLY = Component.translatable("school.testmod.holy").withStyle(Style.EMPTY.withColor(0xfff8d4));
    public static final Component DISPLAY_ENDER = Component.translatable("school.testmod.ender").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final Component DISPLAY_BLOOD = Component.translatable("school.testmod.blood").withStyle(ChatFormatting.DARK_RED);
    public static final Component DISPLAY_EVOCATION = Component.translatable("school.testmod.evocation").withStyle(ChatFormatting.WHITE);
    public static final Component[] DISPLAYS = {DISPLAY_FIRE, DISPLAY_ICE, DISPLAY_LIGHTNING, DISPLAY_HOLY, DISPLAY_ENDER, DISPLAY_BLOOD, DISPLAY_EVOCATION};


}
