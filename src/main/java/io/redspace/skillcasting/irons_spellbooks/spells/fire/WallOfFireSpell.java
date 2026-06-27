package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.irons_spellbooks.component.FireWallCastComponent;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WallOfFireSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(30)
            .build();

    public WallOfFireSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
        );
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
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(3, 40));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, getWallLength(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        FireWallCastComponent data = castContext.getOrNull(SpellcastingComponentTypes.FIRE_WALL_DATA);
        if (data == null) {
            data = new FireWallCastComponent(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, getWallLength(castContext)));
            castContext.set(SpellcastingComponentTypes.FIRE_WALL_DATA, data);
        }
        boolean finishEarly = addAnchor(data, level, castContext);
        if (finishEarly) {
            castContext.getSkillcastingData().recasts().get(this).setRemainingCasts(0);
        }
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        spawnWall(castContext.level(), castContext);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setFireTicks(80);
    }

    private void spawnWall(Level level, CastContext castContext) {
        FireWallCastComponent data = castContext.getOrNull(SpellcastingComponentTypes.FIRE_WALL_DATA);
        if (data == null || data.anchorPoints.isEmpty()) {
            return;
        }
        if (data.anchorPoints.size() == 1) {
            addAnchor(data, level, castContext);
        }
        if (data.anchorPoints.size() <= 1) {
            return;
        }
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        WallOfFireEntity fireWall = new WallOfFireEntity(level, castContext.asEntityCaster(), data.anchorPoints, damage);
        Vec3 origin = Vec3.ZERO;
        for (Vec3 anchor : data.anchorPoints) {
            origin = origin.add(anchor);
        }
        fireWall.setPos(origin.scale(1f / data.anchorPoints.size()));
        level.addFreshEntity(fireWall);
    }

    private float getWallLength(CastContext castContext) {
        return 10 + castContext.getSkillLevel() * 3 * getSpellPowerMultiplier(castContext);
    }

    /**
     * @return true when the wall length budget is exhausted and the recast chain should finish immediately
     */
    private boolean addAnchor(FireWallCastComponent data, Level level, CastContext castContext) {
        Vec3 anchor = resolveAnchor(level, castContext);
        anchor = setOnGround(anchor, level);
        var anchorPoints = data.anchorPoints;
        if (anchorPoints.isEmpty()) {
            anchorPoints.add(anchor);
        } else {
            int i = anchorPoints.size();
            float distance = (float) anchorPoints.get(i - 1).distanceTo(anchor);
            float maxDistance = data.maxTotalDistance - data.accumulatedDistance;
            if (distance <= maxDistance) {
                data.accumulatedDistance += distance;
                anchorPoints.add(anchor);
            } else {
                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
                anchor = setOnGround(anchor, level);
                anchorPoints.add(anchor);
                MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 5, 0.05, 0.25, 0.05, 0, true);
                return true;
            }
        }
        MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 5, 0.05, 0.25, 0.05, 0, true);
        return false;
    }

    private Vec3 resolveAnchor(Level level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity living) {
            return Utils.getTargetBlock(level, living, ClipContext.Fluid.ANY, 20).getLocation();
        }
        HitResult hit = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 20)
                .checkForBlocks(true)
                .build();
        return hit.getLocation();
    }

    private Vec3 setOnGround(Vec3 in, Level level) {
        if (level.getBlockState(BlockPos.containing(in.x, in.y + 0.5f, in.z)).isAir()) {
            for (int i = 0; i < 15; i++) {
                if (!level.getBlockState(BlockPos.containing(in.x, in.y - i, in.z)).isAir()) {
                    return new Vec3(in.x, in.y - i + 1, in.z);
                }
            }
            return new Vec3(in.x, in.y - 15, in.z);
        } else {
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
            return new Vec3(in.x, y, in.z);
        }
    }
}
