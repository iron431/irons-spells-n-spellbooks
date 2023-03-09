package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.spells.SpellRarity;

public class SpellConfigParameters {
    public final int MAX_LEVEL;
    public final SpellRarity MIN_RARITY;

     //Not implemented:
    public final boolean ENABLED;
    public final int BASE_MANA_COST;
    public final int MANA_COST_PER_LEVEL;
    public final int BASE_POWER;
    public final int POWER_PER_LEVEL;

    public SpellConfigParameters(int MAX_LEVEL, SpellRarity MIN_RARITY) {
        this.MAX_LEVEL = MAX_LEVEL;
        this.MIN_RARITY = MIN_RARITY;

        this.ENABLED = true;
        this.BASE_MANA_COST = 0;
        this.MANA_COST_PER_LEVEL = 0;
        this.BASE_POWER = 0;
        this.POWER_PER_LEVEL = 0;
    }
}
