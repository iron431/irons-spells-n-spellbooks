package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PoisonBreathSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public PoisonBreathSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.cast_range",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 2))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.POISON_BREATH_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 1 + getSpellPower(castContext) * 0.75f);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 8f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Set<Entity> entities = SkillcastingUtils.collectConeTargets(castContext,
                target -> target.canBeHitByProjectile() && !DamageSources.isFriendlyFireBetween(castContext.asEntityCaster(), target));
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        entities.forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                DamageSources.ignoreNextKnockback(livingEntity);
            }
            if (DamageSources.applyDamage(entity, damage, getDamageSource(level, null, castContext.asEntityCaster()))
                    && entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            }
        });
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of(this::spawnParticles);
    }

    private void spawnParticles(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast) {
        CastContext castContext = activeCast.context();
        Vec3 rotation = castContext.direction();
        var pos = castContext.position(PositionAnchor.CASTING_POSITION_CENTER).add(rotation.scale(1.5));
        float rangeMultiplier = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 8f) / 8f;

        for (int i = 0; i < 20; i++) {
            double speed = casterRef.level().getRandom().nextDouble() * 0.4 + 0.45;
            speed *= rangeMultiplier;
            double offset = 0.25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = 0.8;
            Vec3 randomVec = new Vec3(
                    Math.random() * 2 * angularness - angularness,
                    Math.random() * 2 * angularness - angularness,
                    Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = rotation.scale(3).add(randomVec).normalize().scale(speed);
            casterRef.level().addParticle(
                    casterRef.level().getRandom().nextFloat() < 0.25f ? ParticleHelper.ACID_BUBBLE : ParticleHelper.ACID,
                    pos.x + ox, pos.y + oy, pos.z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
