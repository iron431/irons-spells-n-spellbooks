package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class HeatSurgeSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(45)
            .build();

    public HeatSurgeSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.rend", Utils.stringTruncation((amplifier + 1) * 5, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 2))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.HEAT_SURGE_PREPARE).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 6 + castContext.getSkillLevel() * 0.5f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 1 + castContext.getSkillLevel());
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        Vec3 position = castContext.position(PositionAnchor.CENTER).add(0, 0.165, 0);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SchoolRegistry.FIRE.get().getTargetingColor(), radius),
                position.x, position.y, position.z, 1, 0, 0, 0, 0, true);
        castContext.caster().distributeToClients(new ShockwaveParticlesPacket(new Vec3(position.x, position.y, position.z), radius, ParticleRegistry.FIRE_PARTICLE.get()));
        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0);
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        float radiusSqr = radius * radius;
        level.getEntities(castContext.asEntityCaster(), AABB.ofSize(position, radius * 2, 4, radius * 2),
                        target -> !DamageSources.isFriendlyFireBetween(target, castContext.asEntityCaster())
                                && Utils.hasLineOfSight(level, position, target.getBoundingBox().getCenter(), true))
                .forEach(target -> {
                    if (target instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(position) < radiusSqr) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.REND, duration, amplifier));
                        livingEntity.setRemainingFireTicks(Math.min(duration / 2, 160));
                        MagicManager.spawnParticles(level, ParticleHelper.EMBERS, livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() * 0.5f, livingEntity.getZ(),
                                50, livingEntity.getBbWidth() * 0.5f, livingEntity.getBbHeight() * 0.5f, livingEntity.getBbWidth() * 0.5f, 0.03, false);
                    }
                });
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}
