package com.example.testmod.spells;

import com.example.testmod.config.ServerConfigs;

public enum CastSource {
    SpellBook,
    Scroll,
    Sword,
    Mob;

    public boolean consumesMana() {
        return this == SpellBook || (this == Sword && ServerConfigs.SWORDS_CONSUME_MANA.get());
    }
}
