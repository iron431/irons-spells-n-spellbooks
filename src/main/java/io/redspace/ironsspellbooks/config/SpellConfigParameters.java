package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.spells.SpellRarity;

public class SpellConfigParameters {
    public final int MAX_LEVEL;
    public final SpellRarity MIN_RARITY;
    public final double MANA_MULTIPLIER;
    public final double POWER_MULTIPLIER;
    public final double COOLDOWN_IN_SECONDS;

    //Not implemented:
    public final boolean ENABLED;

    public SpellConfigParameters(boolean ENABLED, int MAX_LEVEL, SpellRarity MIN_RARITY, double POWER_MULTIPLIER, double MANA_MULTIPLIER, double COOLDOWN_IN_SECONDS) {
        this.MAX_LEVEL = MAX_LEVEL;
        this.MIN_RARITY = MIN_RARITY;

        this.ENABLED = ENABLED;
        this.MANA_MULTIPLIER = MANA_MULTIPLIER;
        this.POWER_MULTIPLIER = POWER_MULTIPLIER;
        this.COOLDOWN_IN_SECONDS = COOLDOWN_IN_SECONDS;
    }
}
