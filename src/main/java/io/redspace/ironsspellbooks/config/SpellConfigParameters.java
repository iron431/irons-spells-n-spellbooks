package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;

public class SpellConfigParameters {
    public final int MAX_LEVEL;
    public final SpellRarity MIN_RARITY;
    public final double MANA_MULTIPLIER;
    public final double POWER_MULTIPLIER;
    public final double COOLDOWN_IN_SECONDS;
    public final boolean ENABLED;
    public final boolean CAN_BE_CRAFTED;

    //Not implemented:

    public SpellConfigParameters(boolean ENABLED, int MAX_LEVEL, SpellRarity MIN_RARITY, double POWER_MULTIPLIER, double MANA_MULTIPLIER, double COOLDOWN_IN_SECONDS, boolean CAN_BE_CRAFTED) {
        //IronsSpellbooks.LOGGER.debug("CFG: SpellConfigParameters");
        this.MAX_LEVEL = MAX_LEVEL;
        this.MIN_RARITY = MIN_RARITY;
        this.ENABLED = ENABLED;
        this.MANA_MULTIPLIER = MANA_MULTIPLIER;
        this.POWER_MULTIPLIER = POWER_MULTIPLIER;
        this.COOLDOWN_IN_SECONDS = COOLDOWN_IN_SECONDS;
        this.CAN_BE_CRAFTED = CAN_BE_CRAFTED;
    }
}
