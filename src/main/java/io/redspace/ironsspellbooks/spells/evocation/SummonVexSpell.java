package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedVex;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SummonVexSpell extends AbstractSpell {
    public SummonVexSpell() {
        this(1);
    }
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", this.level)
        );
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(5)
            .setCooldownSeconds(150)
            .build();

    public SummonVexSpell(int level) {
        super(SpellType.SUMMON_VEX_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 50;
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
        int summonTime = 20 * 60 * 10;
        for (int i = 0; i < this.level; i++) {
            SummonedVex vex = new SummonedVex(world, entity);
            vex.moveTo(entity.getEyePosition().add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
            vex.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(vex.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
            vex.addEffect(new MobEffectInstance(MobEffectRegistry.VEX_TIMER.get(), summonTime, 0, false, false, false));
            world.addFreshEntity(vex);
        }
        int effectAmplifier = level - 1;
        if(entity.hasEffect(MobEffectRegistry.VEX_TIMER.get()))
            effectAmplifier += entity.getEffect(MobEffectRegistry.VEX_TIMER.get()).getAmplifier() + 1;
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.VEX_TIMER.get(), summonTime, effectAmplifier, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }
}
