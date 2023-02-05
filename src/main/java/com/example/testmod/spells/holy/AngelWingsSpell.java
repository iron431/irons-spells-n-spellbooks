package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class AngelWingsSpell extends AbstractSpell {
    public AngelWingsSpell() {
        this(1);
    }

    public AngelWingsSpell(int level) {
        super(SpellType.ANGEL_WING_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
        this.cooldown = 400;
    }

    private int getEffectDuration(LivingEntity entity) {
        return (int) getSpellPower(entity) * 50;
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
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ANGEL_WINGS.get(), getEffectDuration(entity)), entity);

        if (entity instanceof ServerPlayer) {
            playerMagicData.getSyncedData().setHasAngelWings(true);
        }
        super.onCast(world, entity, playerMagicData);
    }
}
