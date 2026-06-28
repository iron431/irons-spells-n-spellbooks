package io.redspace.skillcasting.irons_spellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TelekinesisData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class TelekinesisSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(35)
            .build();

    public TelekinesisSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 4;
        this.castTime = 140;
        this.baseManaCost = 25;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1)));
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
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.TELEKINESIS_CAST).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.TELEKINESIS_LOOP).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public Vector3f getAccentColor() {
        return new Vector3f(1f, 0.24f, 0.95f);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.CAST_TIME, castTime + 20 * (level - 1));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 12f + (level - 1) * 2f);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        int range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 12f).intValue();
        if (!SkillcastingUtils.preCastTargetHelper(castContext, range, 0.15f)) {
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
        float distance = (float) castContext.position(PositionAnchor.ORIGIN).distanceTo(target.position());
        castContext.set(SpellcastingComponentTypes.TELEKINESIS_DATA, new TelekinesisData(distance, target, 6));
        return true;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
    }

    @Override
    public void onServerCastTick(CastContext castContext) {
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        long gameTime = serverLevel.getGameTime();
        int remaining = castContext.getSkillcastingData().castDurationRemaining(gameTime);
        if (remaining % 2 != 0) {
            return;
        }
        handleTelekinesis(castContext, 0.6f, remaining);
    }

    private void handleTelekinesis(CastContext castContext, float strength, int castDurationRemaining) {
        if (!(castContext.level() instanceof ServerLevel world)) {
            return;
        }
        TelekinesisData targetData = castContext.getOrNull(SpellcastingComponentTypes.TELEKINESIS_DATA);
        if (targetData == null) {
            return;
        }
        LivingEntity targetEntity = targetData.getTarget(world);
        if (targetEntity == null) {
            return;
        }
        if (targetEntity.isRemoved() || targetEntity.isDeadOrDying()) {
            SkillcastingManager.cancelCast(castContext.caster(), CastEndReason.INTERRUPTED);
            return;
        }

        Vec3 casterPos = castContext.position(PositionAnchor.ORIGIN);
        Vec3 forward = castContext.direction();

        float resistance = Mth.clamp(1 - (float) targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.2f, 1f);
        float lockedDistance = targetData.getDistance();
        float actualDistance = (float) casterPos.distanceTo(targetEntity.position());
        float distance = Mth.lerp(actualDistance > lockedDistance ? 0.25f : 0.1f, lockedDistance, actualDistance);
        targetData.setDistance(distance);

        Vec3 force = forward.normalize()
                .scale(targetData.getDistance())
                .add(casterPos)
                .subtract(targetEntity.position())
                .scale(resistance * strength);
        Vec3 travel = new Vec3(targetEntity.getX() - targetEntity.xOld, targetEntity.getY() - targetEntity.yOld, targetEntity.getZ() - targetEntity.zOld);
        if (force.y > 0) {
            targetEntity.resetFallDistance();
        }
        if (castDurationRemaining % 10 == 0) {
            int airborne = (int) (travel.x * travel.x + travel.z * travel.z) / 2;
            targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.AIRBORNE, 31, airborne));
            targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.ANTIGRAVITY, 11, 0));
        }
        Vec3 deltaMovement = targetEntity.getDeltaMovement();
        Vec3 newMotion = force.subtract(deltaMovement).scale(0.25).add(deltaMovement);
        Vec3 delta = newMotion.subtract(deltaMovement);
        Vec3 clampedMotion = new Vec3(
                Utils.signedMin(delta.x, force.x * 4),
                Utils.signedMin(delta.y, force.y * 4),
                Utils.signedMin(delta.z, force.z * 4)
        );
        targetEntity.setDeltaMovement(deltaMovement.add(clampedMotion));
        targetEntity.hurtMarked = true;
    }
}
