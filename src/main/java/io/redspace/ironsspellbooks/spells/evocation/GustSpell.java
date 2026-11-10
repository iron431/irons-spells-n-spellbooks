package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.skillcasting.animation.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.AirborneEffect;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GustSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float strength = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f) * 0.2f;
        float levelOneStrength = baseSpellPower * 0.2f;
        return List.of(
                Component.translatable("ui.irons_spellbooks.strength", String.format("%s%%", (int) (strength * 100 / levelOneStrength))),
                Component.translatable("ui.irons_spellbooks.impact_damage", Utils.stringTruncation(AirborneEffect.getDamageFromLevel(castContext.getSkillLevel()), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public GustSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 30;
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
    public Optional<PlayableSound> provideCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.GUST_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> provideOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.GUST_CAST).toOpt();
    }

    @Override
    public AnimationHolder provideCastStartAnimation(CastContext castContext) {
        return SpellAnimations.CHARGE_WAVY_ANIMATION;
    }

    @Override
    public AnimationHolder provideCastFinishAnimation(CastContext castContext) {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 8f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float strength = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f) * 0.2f;
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        Entity caster = castContext.entityOwner();
        Vec3 casterPos = castContext.position(PositionAnchor.BOTTOM_CENTER);
        Set<Entity> targets = SkillcastingUtils.collectConeTargets(castContext, target -> !DamageSources.isFriendlyFireBetween(caster, target));
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity target) {
                Vec3 knockback = target.position().subtract(casterPos).normalize().scale(strength);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffectRegistry.AIRBORNE, 60, amplifier));
            }
        }

        Vec3 direction = castContext.direction();
        Vec3 gustPos = castContext.position(PositionAnchor.CENTER).add(direction.scale(2f));
        MagicManager.spawnParticles(level, ParticleRegistry.GUST_PARTICLE.get(), gustPos.x, gustPos.y, gustPos.z, 0, direction.x, direction.y, direction.z, 1, true);

        if (caster instanceof LivingEntity living) {
            Vec3 center = castContext.position(PositionAnchor.CENTER);
            float kickback = (float) center.distanceToSqr(Utils.raycastForBlock(level, center, center.add(direction.scale(3.5f)), ClipContext.Fluid.NONE).getLocation());
            kickback = Mth.clamp(1 / (kickback + 1) - .11f, 0f, .95f);
            if (kickback > 0) {
                living.setDeltaMovement(living.getDeltaMovement().subtract(direction.scale(kickback * castContext.getSkillLevel() * .25f)));
                living.resetFallDistance();
                living.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
