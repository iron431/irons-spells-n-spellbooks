package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.util.ModTags;
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
    EVOCATION(6),
    VOID(7),
    POISON(8);

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
        } else if (stack.is(ModTags.EVOCATION_FOCUS)) {
            return EVOCATION;
        }else if (stack.is(ModTags.VOID_FOCUS)) {
            return VOID;
        }else if (stack.is(ModTags.POISON_FOCUS)) {
            return POISON;
        } else return null;
    }

    public static final Component DISPLAY_FIRE = Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD);
    public static final Component DISPLAY_ICE = Component.translatable("school.irons_spellbooks.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff));
    public static final Component DISPLAY_LIGHTNING = Component.translatable("school.irons_spellbooks.lightning").withStyle(ChatFormatting.AQUA);
    public static final Component DISPLAY_HOLY = Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4));
    public static final Component DISPLAY_ENDER = Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final Component DISPLAY_BLOOD = Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED);
    public static final Component DISPLAY_EVOCATION = Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE);
    public static final Component DISPLAY_VOID = Component.translatable("school.irons_spellbooks.void").withStyle(Style.EMPTY.withColor(0x490059));
    public static final Component DISPLAY_POISON = Component.translatable("school.irons_spellbooks.poison").withStyle(ChatFormatting.GREEN);
    public static final Component[] DISPLAYS = {DISPLAY_FIRE, DISPLAY_ICE, DISPLAY_LIGHTNING, DISPLAY_HOLY, DISPLAY_ENDER, DISPLAY_BLOOD, DISPLAY_EVOCATION, DISPLAY_VOID, DISPLAY_POISON};


}
