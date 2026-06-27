package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SlowSpell extends AbstractSpellSkill {

    private static final int MAX_TARGETS = 5;

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.slowed", Utils.stringTruncation((1 + amplifier) * .1f * 100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)),
                Component.translatable("ui.irons_spellbooks.max_victims", MAX_TARGETS)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(80)
            .build();

    public SlowSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 4;
        this.castTime = 30;
        this.baseManaCost = 50;
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_SUMMON).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        if (!SkillcastingUtils.preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f).intValue(), 0.35f, true)) {
            return false;
        }
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        LivingEntity target = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
        if (target == null) {
            return false;
        }
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        TargetedAreaEntity area = TargetedAreaEntity.createTargetAreaEntity(
                castContext.level(),
                target.position(),
                radius,
                MobEffectRegistry.SLOWED.get().getColor(),
                target);
        castContext.set(SkillcastingComponentTypes.ATTACHED_ENTITIES, new MultiTargetEntityCastComponent(area));
        return true;
    }

    @Override
    public void onServerCastComplete(CastContext castContext, CastEndReason reason) {
        super.onServerCastComplete(castContext, reason);
        if (castContext.level() instanceof ServerLevel serverLevel) {
            castContext.find(SkillcastingComponentTypes.ATTACHED_ENTITIES).ifPresent(
                    entities -> entities.getTargets().forEach(uuid -> {
                        if (serverLevel.getEntity(uuid) instanceof TargetedAreaEntity targetEntity) {
                            targetEntity.discard();
                        }
                    })
            );
        }
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        LivingEntity targetEntity = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
        if (targetEntity == null) {
            return;
        }

        Entity caster = castContext.asEntityCaster();
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0);
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        int targets = 0;

        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(radius))) {
            if (targets >= MAX_TARGETS) {
                break;
            }
            if (victim != caster && victim.distanceToSqr(targetEntity) < radius * radius && !DamageSources.isFriendlyFireBetween(caster, victim)) {
                victim.addEffect(new MobEffectInstance(MobEffectRegistry.SLOWED, duration, amplifier));
                targets++;
            }
        }
    }
}
