package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.network.particles.FrostStepParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class FrostStepSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.shatter_damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(12)
            .build();

    public FrostStepSpell() {
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 15;
        this.manaCostPerLevel = 5;
        this.castTime = 0;
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
        return Optional.of(PlayableSound.of(SoundRegistry.FROST_STEP, 1f, .9f, 1.1f));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, getDistance(castContext));
        castContext.set(SkillcastingComponentTypes.DAMAGE, castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER, 0f));

    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof LivingEntity entity)) {
            // todo: maybe support other caster types, but certainly not blocks
            //  spells doing this frequently is the sign of a deeper issue. sun tzu said that.
            return;
        }
        FrozenHumanoid shadow = new FrozenHumanoid(level, entity);
        shadow.setShatterDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        shadow.setDeathTimer(100);
        level.addFreshEntity(shadow);

        var tauntTarget = entity.getLastHurtByMob();
        Predicate<Entity> predicate = tauntTarget == null ? (mob -> entity instanceof Enemy ^ mob instanceof Enemy)
                : (mob -> mob.getClass().isAssignableFrom(tauntTarget.getClass()) || mob.isAlliedTo(tauntTarget) || entity instanceof Enemy ^ mob instanceof Enemy);
        Utils.performTaunt(shadow, 10, predicate);

        Vec3 targetPos = castContext.getOrNull(SkillcastingComponentTypes.TARGET_POSITION);
        if (targetPos == null) {
            targetPos = findTeleportLocation(level, entity, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f));
        }
        castContext.caster().distributeToClients(new FrostStepParticlesPacket(castContext.position(PositionAnchor.ORIGIN), targetPos));

        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        //fixme: full skill takeover
        Utils.handleSpellTeleport(null/*this*/, entity, targetPos);
        entity.resetFallDistance();
        Vec3 soundPos = targetPos;
        getOnCastSound(castContext).ifPresent(playableSound -> level.playSound(null, soundPos.x, soundPos.y, soundPos.z, playableSound.soundEventHolder().value(), SoundSource.NEUTRAL, 1f, 1f));
    }

    private Vec3 findTeleportLocation(Level level, LivingEntity entity, float distance) {
        return TeleportSpell.findTeleportLocation(level, entity, distance);
    }

    public static void particleCloud(Level level, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = 1;
            for (int i = 0; i < 25; i++) {
                double x = pos.x + Utils.random.nextDouble() * width * 2 - width;
                double y = pos.y + height + Utils.random.nextDouble() * height * 1.2 * 2 - height * 1.2;
                double z = pos.z + Utils.random.nextDouble() * width * 2 - width;
                double dx = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dy = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dz = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                level.addParticle(ParticleHelper.SNOWFLAKE, true, x, y, z, dx, dy, dz);
                level.addParticle(ParticleTypes.SNOWFLAKE, true, x, y, z, -dx, -dy, -dz);
            }
        }
    }

    private float getDistance(CastContext castContext) {
        //fixme: teleport balance + entity power multipliers
        return 9 + castContext.getSkillLevel() * 1.5f;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }
}
