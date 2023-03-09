package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class NoneSpell extends AbstractSpell {
    public NoneSpell() {
        this(0);
    }

    public NoneSpell(int level) {
        super(SpellType.NONE_SPELL);
        this.level = level;
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.cooldown = 0;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {

    }
}
