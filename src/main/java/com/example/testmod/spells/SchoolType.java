package com.example.testmod.spells;

import com.example.testmod.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    public static SchoolType getSchoolFromItem(Item item) {
        if (Items.BLAZE_POWDER.equals(item)) {
            return FIRE;
        } else if (ItemRegistry.FROZEN_BONE_SHARD.get().equals(item)) {
            return ICE;
        }else if (ItemRegistry.LIGHTNING_BOTTLE.get().equals(item)) {
            return LIGHTNING;
        }else if (Items.AMETHYST_SHARD.equals(item)) {
            return HOLY;
        }else if (Items.ENDER_PEARL.equals(item)) {
            return ENDER;
        }else if (ItemRegistry.BLOOD_VIAL.get().equals(item)) {
            return BLOOD;
        }else if (Items.EMERALD.equals(item)) {
            return EVOCATION;
        }/*else if (Items.ECHO_SHARD.equals(item)) {
            return VOID;
        }*/
        else return null;
    }
    public static SchoolType getSchoolFromItem(ItemStack stack){
        return getSchoolFromItem(stack.getItem());
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
