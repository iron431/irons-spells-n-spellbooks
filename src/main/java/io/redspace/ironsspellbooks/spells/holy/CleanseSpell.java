package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class CleanseSpell extends AbstractSpell {

    private static final float RADIUS = 3f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public CleanseSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 60;
        this.baseManaCost = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.radius", (int) RADIUS));
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.CLEANSE_CAST).toOpt();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        int channelTicks = castContext.getOrDefault(SkillcastingComponentTypes.CAST_TIME, castTime);
        TargetedAreaEntity area = TargetedAreaEntity.createTargetAreaEntity(
                castContext.level(), castContext.position(), RADIUS,
                Utils.packRGB(getSchoolType().getTargetingColor()), castContext.asEntityCaster());
        area.setDuration(channelTicks);
        castContext.set(SkillcastingComponentTypes.ATTACHED_ENTITIES, new TargetedEntitiesData(area));
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
                    }));
        }
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        Vec3 center = castContext.position();
        level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(center, 6, 6, 6))
                .forEach(livingEntity -> {
                    if (Utils.shouldHealEntity(caster, livingEntity)) {
                        var effects = livingEntity.getActiveEffects().stream()
                                .map(MobEffectInstance::getEffect)
                                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL
                                        && !effect.is(ModTags.CLEANSE_IMMUNE))
                                .toList();
                        effects.forEach(livingEntity::removeEffect);
                        MagicManager.spawnParticles(level, ParticleHelper.CLEANSE_PARTICLE,
                                livingEntity.getX(), livingEntity.getY() + 0.25, livingEntity.getZ(),
                                15, livingEntity.getBbWidth() * 0.5, livingEntity.getBbWidth() * 0.5,
                                livingEntity.getBbWidth() * 0.5, 0, false);
                    }
                });
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CAST_KNEELING_PRAYER;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_TWO_HANDS;
    }
}
