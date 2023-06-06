package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class CounterspellSpell extends AbstractSpell {
    public static final List<MobEffect> MAGICAL_EFFECTS = List.of(/*MobEffectRegistry.ABYSSAL_SHROUD.get(), MobEffectRegistry.ASCENSION.get(), MobEffectRegistry.ANGEL_WINGS.get(), MobEffectRegistry.CHARGED.get(), MobEffectRegistry.EVASION.get(), MobEffectRegistry.HEARTSTOP.get(), MobEffectRegistry.FORTIFY.get(), MobEffectRegistry.TRUE_INVISIBILITY.get()*/);
    public CounterspellSpell() {
        this(1);
    }

    public CounterspellSpell(int level) {
        super(SpellType.COUNTERSPELL_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;

    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.ENDER)
            .setMaxLevel(1)
            .setCooldownSeconds(15)
            .build();

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
        Vec3 start = entity.getEyePosition();
        Vec3 end = start.add(entity.getForward().normalize().scale(80));
        HitResult hitResult = Utils.raycastForEntity(entity.level, entity, start, end, true, 0.35f, Utils::validAntiMagicTarget);
        Vec3 forward = entity.getForward().normalize();
        if (hitResult instanceof EntityHitResult entityHitResult) {
            double distance = entity.distanceTo(entityHitResult.getEntity());
            for (float i = 1; i < distance; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            }
            if (entityHitResult.getEntity() instanceof AntiMagicSusceptible antiMagicSusceptible && !(antiMagicSusceptible instanceof MagicSummon summon && summon.getSummoner() == entity))
                antiMagicSusceptible.onAntiMagic(playerMagicData);
            else if (entityHitResult.getEntity() instanceof ServerPlayer serverPlayer)
                Utils.serverSideCancelCast(serverPlayer, true);
            else if (entityHitResult.getEntity() instanceof AbstractSpellCastingMob abstractSpellCastingMob)
                abstractSpellCastingMob.cancelCast();

            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity)
                for (MobEffect mobEffect : MAGICAL_EFFECTS)
                    livingEntity.removeEffect(mobEffect);
        }else{
            for (float i = 1; i < 40; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
                if (!world.getBlockState(BlockPos.containing(pos)).isAir())
                    break;
            }
        }
        super.onCast(world, entity, playerMagicData);
    }


}