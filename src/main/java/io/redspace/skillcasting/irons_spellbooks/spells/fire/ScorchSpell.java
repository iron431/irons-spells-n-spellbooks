package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireField;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ScorchSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public ScorchSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.AOE_DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
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
        return PlayableSound.standard(SoundRegistry.SCORCH_PREPARE).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 2.5f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.AOE_DAMAGE, getSpellPower(castContext) * 0.1f);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 2.5f);
        var hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f))
                .checkForBlocks(true)
                .bbInflation(0.2f)
                .build();
        Vec3 location = Utils.moveToRelativeGroundLevel(castContext.level(), hitResult.getLocation(), 3, 6);
        castContext.set(SkillcastingComponentTypes.TARGET_POSITION, location);
        int channelTicks = castContext.getOrDefault(SkillcastingComponentTypes.CAST_TIME, castTime);
        TargetedAreaEntity area = TargetedAreaEntity.createTargetAreaEntity(
                castContext.level(), location, radius, Utils.packRGB(getSchoolType().getTargetingColor()));
        area.setDuration(channelTicks);
        castContext.set(SkillcastingComponentTypes.ATTACHED_ENTITIES, new MultiTargetEntityCastComponent(area));
        return true;
    }

    @Override
    public void onServerCastComplete(CastContext castContext, CastEndReason reason) {
        super.onServerCastComplete(castContext, reason);
        if (castContext.level() instanceof ServerLevel serverLevel) {
            castContext.find(SkillcastingComponentTypes.ATTACHED_ENTITIES).ifPresent(
                    entities -> entities.getTargets().forEach(uuid -> {
                        if (serverLevel.getEntity(uuid) instanceof TargetedAreaEntity targetEntity)
                            targetEntity.discard();
                    })
            );
        }
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 targetArea = castContext.getOrNull(SkillcastingComponentTypes.TARGET_POSITION);
        if (targetArea == null) {
            var hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f))
                    .checkForBlocks(true)
                    .bbInflation(0.2f)
                    .build();
            targetArea = Utils.moveToRelativeGroundLevel(level, hitResult.getLocation(), 3, 6);
        }
        MagicManager.spawnParticles(level, ParticleTypes.LAVA, targetArea.x, targetArea.y, targetArea.z, 25, 1, 1, 1, 1, true);
        MagicManager.spawnParticles(level, ParticleTypes.LAVA, targetArea.x, targetArea.y + 1, targetArea.z, 25, 0.25, 1.5, 0.25, 1, false);
        PlayableSound.of(SoundRegistry.FIERY_EXPLOSION, 2f, 0.8f, 1.2f).play(level, targetArea, SoundSource.PLAYERS);

        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 2.5f);
        float radiusSqr = radius * radius;
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        Entity caster = castContext.asEntityCaster();
        var source = getDamageSource(level, null, caster);
        Vec3 _targetArea = targetArea;
        level.getEntitiesOfClass(LivingEntity.class, new AABB(targetArea.subtract(radius, radius, radius), targetArea.add(radius, radius, radius)),
                        livingEntity -> livingEntity != caster &&
                                horizontalDistanceSqr(livingEntity, _targetArea) < radiusSqr &&
                                livingEntity.isPickable() &&
                                !DamageSources.isFriendlyFireBetween(livingEntity, caster) &&
                                Utils.hasLineOfSight(level, _targetArea.add(0, 1.5, 0), livingEntity.getBoundingBox().getCenter(), true))
                .forEach(livingEntity -> {
                    DamageSources.applyDamage(livingEntity, damage, source);
                    DamageSources.ignoreNextKnockback(livingEntity);
                });

        FireField fire = new FireField(level);
        fire.setOwner(caster);
        fire.setDuration(200);
        fire.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.AOE_DAMAGE, 0f));
        fire.setRadius(radius);
        fire.setCircular();
        fire.moveTo(targetArea);
        level.addFreshEntity(fire);
    }

    private float horizontalDistanceSqr(LivingEntity livingEntity, Vec3 vec3) {
        double dx = livingEntity.getX() - vec3.x;
        double dz = livingEntity.getZ() - vec3.z;
        return (float) (dx * dx + dz * dz);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setFireTicks(60);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_INSTANT_CAST;
    }
}
