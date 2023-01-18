package com.example.testmod.config;

import com.example.testmod.spells.SpellRarity;

import java.util.Arrays;
import java.util.List;

class SpellConfigParameters {
    final int MAX_LEVEL;
    final SpellRarity MIN_RARITY;

    //Not implemented:
    final boolean ENABLED;
    final int BASE_MANA_COST;
    final int MANA_COST_PER_LEVEL;
    final int BASE_POWER;
    final int POWER_PER_LEVEL;

    SpellConfigParameters(int MAX_LEVEL, SpellRarity MIN_RARITY) {
        this.MAX_LEVEL = MAX_LEVEL;
        this.MIN_RARITY = MIN_RARITY;

        this.ENABLED = true;
        this.BASE_MANA_COST = 0;
        this.MANA_COST_PER_LEVEL = 0;
        this.BASE_POWER = 0;
        this.POWER_PER_LEVEL = 0;
    }

    List<Integer> asList() {
        return Arrays.asList(MAX_LEVEL, MIN_RARITY.getValue());
    }
}
