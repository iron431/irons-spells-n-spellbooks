package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.AirborneEffect;
import io.redspace.ironsspellbooks.entity.spells.gust.GustCollider;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;

import java.util.List;
import java.util.Optional;

public class GustSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float strength = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 0f);
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
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.GUST_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.GUST_CAST).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_WAVY_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 8f);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, getSpellPower(castContext) * 0.2f);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 8f);
        float strength = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 0f);

        GustCollider gust = new GustCollider(level, castContext.asEntityCaster());
        gust.setPos(castContext.position(PositionAnchor.CENTER).add(castContext.direction().scale(2f)));
        gust.setYRot(castContext.getYRot() * -Mth.RAD_TO_DEG);
        gust.setXRot(castContext.getXRot() * -Mth.RAD_TO_DEG);
        gust.range = range;
        gust.strength = strength;
        gust.amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        level.addFreshEntity(gust);
        gust.setDealDamageActive();
        gust.tick();

        Entity caster = castContext.asEntityCaster();
        if (caster instanceof LivingEntity living) {
            float kickback = (float) living.getBoundingBox().getCenter().distanceToSqr(Utils.getTargetBlock(level, living, ClipContext.Fluid.NONE, 3.5f).getLocation());
            kickback = Mth.clamp(1 / (kickback + 1) - .11f, 0f, .95f);
            if (kickback > 0) {
                living.setDeltaMovement(living.getDeltaMovement().subtract(living.getLookAngle().scale(kickback * castContext.getSkillLevel() * .25f)));
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
