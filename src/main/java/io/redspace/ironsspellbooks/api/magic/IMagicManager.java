package io.redspace.ironsspellbooks.api.magic;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;

public interface IMagicManager {
    void addCooldown(ServerPlayer serverPlayer, AbstractSpellSkill spell, CastSource castSource);
}
