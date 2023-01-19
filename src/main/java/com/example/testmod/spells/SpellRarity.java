package com.example.testmod.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum SpellRarity {
    COMMON(0),
    UNCOMMON(1),
    RARE(2),
    EPIC(3),
    LEGENDARY(4)/*,
    MYTHIC(5),
    ANCIENT(6)*/;

    private final int value;

    SpellRarity(final int newValue) {
        value = newValue;
    }

    public static SpellRarity getSpellRarity(AbstractSpell spell) {
        //TODO: calcuate this dynamic and based off of config files
        return switch (spell.level) {
            case 1 -> COMMON;
            case 2 -> UNCOMMON;
            case 3 -> RARE;
            case 4 -> EPIC;
            default -> LEGENDARY;
        };
    }

    public int getValue() {
        return this.value;
    }

    public MutableComponent getDisplayName() {
        return DISPLAYS[getValue()];
    }

    /**
     * @return Returns positive if the other is less rare, negative if it is more rare, and zero if they are equal
     */
    public int compareRarity(SpellRarity other) {
        return Integer.compare(this.getValue(), other.getValue());
    }

//    public static SpellRarity getRarityFromPercent(float f){
//        if (f >= .9f)
//            return SpellRarity.LEGENDARY;
//        else if (f >= .8f)
//            return SpellRarity.EPIC;
//        else if (f >= .6f)
//            return SpellRarity.RARE;
//        else if (f >= .4f)
//            return SpellRarity.UNCOMMON;
//        else
//            return SpellRarity.COMMON;
//    }

    private final MutableComponent[] DISPLAYS = {
            Component.translatable("rarity.testmod.common").withStyle(ChatFormatting.GRAY),
            Component.translatable("rarity.testmod.uncommon").withStyle(ChatFormatting.GREEN),
            Component.translatable("rarity.testmod.rare").withStyle(ChatFormatting.BLUE),
            Component.translatable("rarity.testmod.epic").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("rarity.testmod.legendary").withStyle(ChatFormatting.GOLD),
            Component.translatable("rarity.testmod.mythic").withStyle(ChatFormatting.GOLD),
            Component.translatable("rarity.testmod.ancient").withStyle(ChatFormatting.GOLD),
    };
}
