package io.redspace.ironsspellbooks.api.magic;

import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.server.level.ServerPlayer;

public interface IMagicManager {
    void setPlayerCurrentMana(ServerPlayer serverPlayer, int newManaValue);

    void addCooldown(ServerPlayer serverPlayer, SpellType spellType, CastSource castSource);
}
