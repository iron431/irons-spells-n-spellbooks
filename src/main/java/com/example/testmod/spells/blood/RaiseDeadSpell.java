package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.SummonedZombie;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class RaiseDeadSpell extends AbstractSpell {
    public RaiseDeadSpell() {
        this(1);
    }

    public RaiseDeadSpell(int level) {
        super(SpellType.RAISE_DEAD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 50;
        this.cooldown = 300;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        int summonTime = 20 * 60 * 3;
        for (int i = 0; i < this.level; i++) {
            SummonedZombie zombie = new SummonedZombie(world, entity);
            zombie.setPos(entity.getEyePosition().add(new Vec3(1, 1, 1).yRot(i * 25)));
            zombie.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(zombie.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
            zombie.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, false));
            world.addFreshEntity(zombie);
        }
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }
}
