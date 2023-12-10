package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.config.ServerConfigs;

public enum CastSource {
    SPELLBOOK,
    SCROLL,
    SWORD,
    MOB,
    COMMAND,
    NONE;

    public boolean consumesMana() {
        return this == SPELLBOOK || (this == SWORD && ServerConfigs.SWORDS_CONSUME_MANA.get());
    }

    public boolean respectsCooldown() {
        return this == SPELLBOOK || this == SWORD;
    }

}
