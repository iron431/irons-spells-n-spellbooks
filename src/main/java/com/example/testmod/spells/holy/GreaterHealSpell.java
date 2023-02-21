package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.network.spell.ClientboundHealParticles;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
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
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 200;
        this.baseManaCost = 30;
        this.cooldown = 400;
        uniqueInfo.add(Component.translatable("ui.testmod.greater_healing"));
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
