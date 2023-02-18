package com.example.testmod.spells;

import com.example.testmod.config.ServerConfigs;

public enum CastSource {
    SPELLBOOK,
    SCROLL,
    SWORD,
    MOB,
    NONE;

    public boolean consumesMana() {
        return this == SPELLBOOK || (this == SWORD && ServerConfigs.SWORDS_CONSUME_MANA.get());
    }

}
