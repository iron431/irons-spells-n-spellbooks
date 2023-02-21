package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.network.spell.ClientboundHealParticles;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CloudOfRegenerationSpell extends AbstractSpell {
    public CloudOfRegenerationSpell() {
        this(1);
    }

    final float radius = 5;

    public CloudOfRegenerationSpell(int level) {
        super(SpellType.CLOUD_OF_REGENERATION);
        this.level = level;
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 2;
        this.castTime = 200;
        this.baseManaCost = 10;
        this.cooldown = 400;
        uniqueInfo.add(Component.translatable("ui.testmod.healing", Utils.stringTruncation(getHealing(null), 1)));

    }

    private float getHealing(Entity caster) {
        return getSpellPower(caster) * .5f;
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
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius)).forEach((target) -> {
            if (target.distanceToSqr(entity.position()) < radius * radius) {
                target.heal(getHealing(entity));
                MagicManager.distrobuteParticlePacket(level, new ClientboundHealParticles(target.position()));
            }
        });
        super.onCast(level, entity, playerMagicData);
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        super.onClientPreCast(level, entity, hand, playerMagicData);
    }
}
