package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class BloodStepSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(12)
            .build();

    public BloodStepSpell() {
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 4;
        this.baseManaCost = 30;
        this.manaCostPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1)));
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.BLOOD_STEP).toOpt();
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE,
                (float) (Utils.softCapFormula(getSpellPowerMultiplier(castContext)) * getSpellPower(castContext)));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 100);
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        CasterRef caster = castContext.caster();
        Vec3 forward = castContext.direction().normalize();
        var level = caster.level();
        var origin = castContext.position(PositionAnchor.ORIGIN);
        for (int i = 0; i < 35; i++) {
            Vec3 motion = forward.scale(level.random.nextDouble() * 0.25);
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    origin.x + (level.random.nextFloat() - 0.5f) * 0.4f,
                    origin.y + level.random.nextFloat(),
                    origin.z + (level.random.nextFloat() - 0.5f) * 0.4f,
                    motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof LivingEntity entity)) {
            return;
        }
        Vec3 dest = castContext.getOrNull(SkillcastingComponentTypes.TARGET_POSITION);
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 12f);

        if (dest == null) {
            HitResult hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, range)
                    .checkForBlocks(true)
                    .build();
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof LivingEntity target) {
                for (int i = 0; i < 8; i++) {
                    dest = target.position().subtract(new Vec3(0, 0, 1.5).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD));
                    if (level.getBlockState(BlockPos.containing(dest).above()).isAir()) {
                        break;
                    }
                }
                Utils.handleSpellTeleport(null, entity, dest.add(0, 1, 0));
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition().subtract(0, 0.15, 0));
            } else {
                dest = TeleportSpell.findTeleportLocation(level, entity, range);
                Utils.handleSpellTeleport(null, entity, dest);
            }
        } else {
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            Utils.handleSpellTeleport(null, entity, dest);
        }

        entity.resetFallDistance();
        Vec3 soundPos = dest;
        getOnCastSound(castContext).ifPresent(sound -> level.playSound(null, soundPos.x, soundPos.y, soundPos.z,
                sound.soundEventHolder().value(), SoundSource.NEUTRAL, 1f, 1f));

        entity.setInvisible(true);
        entity.addEffect(new MobEffectInstance(
                MobEffectRegistry.TRUE_INVISIBILITY,
                castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 100),
                0,
                false,
                false,
                true));
    }
}
