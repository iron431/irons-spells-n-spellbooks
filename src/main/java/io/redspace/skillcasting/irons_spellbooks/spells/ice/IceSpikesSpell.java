package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ice_spike.IceSpikeEntity;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IceSpikesSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.spike_count", castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f).intValue())
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public IceSpikesSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
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
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, (int) (castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f) * 1.25f), 0.15f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 7 + 3 * castContext.getSkillLevel() / 2.0f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 forward = castContext.direction().multiply(1, 0, 1).normalize();
        Vec3 start = castContext.position().add(forward.scale(1.5));

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float minScale = 1f;
        float maxScale = 3f;
        int count = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f).intValue();
        start = Utils.moveToRelativeGroundLevel(level, start, 1, 3).add(0, 0.1, 0);
        double distance = count;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null) {
            var target = targetData.getFirstEntityTarget((ServerLevel) level);
            if (target != null) {
                distance = start.subtract(target.position()).horizontalDistance();
                Vec3 targetPos = target.position().add(target.getDeltaMovement().multiply(distance, 0, distance));
                distance = targetPos.subtract(start).horizontalDistance();
            }
        }
        float distanceCovered = 0;
        for (int i = 0; i < count; i++) {
            float f = (float) Math.max(i / (float) count, (distanceCovered + 1) / distance);
            float scale = Mth.lerp(f, minScale, maxScale);
            Vec3 spawn = start.add(forward.scale(i));
            var ground = Utils.moveToRelativeGroundLevel(level, spawn, 8);
            spawn = ground.subtract(spawn).scale(Mth.clamp(i / 3f, 0, 1)).add(spawn);
            boolean isFinalSpike = i == count - 1 || distanceCovered + 1 > distance;
            if (isFinalSpike) {
                //the final spike does full damage, the small spikes to half damage
                scale = maxScale * 1.5f;
            }

            distanceCovered += (float) forward.horizontalDistance();
            int delay = i;
            if (level.getBlockState(BlockPos.containing(spawn).below()).isFaceSturdy(level, BlockPos.containing(spawn).below(), Direction.UP)) {
                IceSpikeEntity spike = new IceSpikeEntity(level, castContext.asEntityCaster());
                if (i % 2 == count % 2) {
                    spike.setSilent(true);
                }
                spike.setSpikeSize(scale);
                spike.moveTo(spawn.add(0, 0, 0));
                spike.setWaitTime(delay);
                spike.setDamage(damage * (isFinalSpike ? 1 : 0.5f));
                spike.setYRot((castContext.getYRot() - 45 + Utils.random.nextIntBetweenInclusive(-20, 20)));
                spike.setXRot(Utils.random.nextIntBetweenInclusive(-15, 15));
                level.addFreshEntity(spike);
            }
            if (isFinalSpike) {
                break;
            }
        }
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast activeCast, Mob mob, LivingEntity target) {
        float f = activeCast.context().getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f) * 1.5f;
        return mob.distanceToSqr(target) > (f * f);
    }
}
