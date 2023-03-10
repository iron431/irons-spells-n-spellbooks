package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.network.spell.ClientboundHealParticles;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class GreaterHealSpell extends AbstractSpell {
    public GreaterHealSpell() {
        this(1);
    }

    public GreaterHealSpell(int level) {
        super(SpellType.GREATER_HEAL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 200;
        this.baseManaCost = 100;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.greater_healing"));
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
        entity.heal(entity.getMaxHealth());
        Messages.sendToPlayersTrackingEntity(new ClientboundHealParticles(entity.position()), entity,true);

        super.onCast(world, entity, playerMagicData);
    }
}
