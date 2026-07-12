package io.redspace.skillcasting.util;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.skillcasting.api.AbstractSkillProjectile;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class RaycastBuilder {

    private final Level level;
    private final @Nullable Entity originEntity;
    private Vec3 start;
    private Vec3 end;
    private boolean checkForBlocks = false;
    private float bbInflation = 0;
    private Predicate<Entity> filter = Utils::canHitWithRaycast;

    public RaycastBuilder(Level level, @Nullable Entity originEntity) {
        this.level = level;
        this.originEntity = originEntity;
    }

    public static RaycastBuilder begin(Level level, @Nullable Entity originEntity) {
        return new RaycastBuilder(level, originEntity);
    }

    public static RaycastBuilder fromCast(CastContext castContext, PositionAnchor anchor, float range) {
        return RaycastBuilder.begin(castContext.level(), castContext.asEntityCaster())
                .start(castContext.position(anchor))
                .end(castContext.direction(), range);
    }

    public static RaycastBuilder fromCast(CastContext castContext, PositionAnchor anchor) {
        return fromCast(castContext, anchor, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f));
    }

    public RaycastBuilder start(Vec3 start) {
        this.start = start;
        return this;
    }

    public RaycastBuilder end(Vec3 endPos) {
        this.end = endPos;
        return this;
    }

    public RaycastBuilder end(Vec3 direction, double range) {
        Objects.requireNonNull(this.start, "Cannot project end from start before start is defined.");
        this.end = start.add(direction.normalize().scale(range));
        return this;
    }

    public RaycastBuilder checkForBlocks(boolean checkForBlocks) {
        this.checkForBlocks = checkForBlocks;
        return this;
    }

    public RaycastBuilder bbInflation(float bbInflation) {
        this.bbInflation = bbInflation;
        return this;
    }

    public RaycastBuilder filter(Predicate<Entity> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Executes the raycast with the current parameters. Start and end must be set.
     *
     * @return the hit result (entity hit, block hit, or miss)
     */
    public HitResult build() {
        return performRaycast();
    }

    /**
     * Performs the raycast with the current parameters. Start and end must be set.
     *
     * @return raycast HitResult
     */
    public HitResult performRaycast() {
        Objects.requireNonNull(start, "Start must be set to perform raycast");
        Objects.requireNonNull(end, "End must be set to perform raycast");

        BlockHitResult blockHitResult = null;
        Vec3 rayEnd = end;

        if (checkForBlocks) {
            blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity == null ? CollisionContext.empty() : CollisionContext.of(originEntity)));
            rayEnd = blockHitResult.getLocation();
        }

        AABB range = new AABB(start.x, start.y, start.z, end.x, end.y, end.z).inflate(2);
        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, filter);

        for (Entity target : entities) {
            HitResult hit = Utils.checkEntityIntersecting(target, start, rayEnd, bbInflation);
            if (hit.getType() != HitResult.Type.MISS) {
                hits.add(hit);
            }
        }

        if (!hits.isEmpty()) {
            hits.sort(Comparator.comparingDouble(o -> o.getLocation().distanceToSqr(start)));
            return hits.get(0);
        }
        if (checkForBlocks) {
            return blockHitResult;
        } else {
            return BlockHitResult.miss(rayEnd, Direction.UP, BlockPos.containing(rayEnd));
        }
    }

    /**
     * Performs the raycast with the current parameters. Start and end be been set.
     *
     * @return raycast HitResults
     */
    public List<HitResult> performRaycastWithRicochet(int ricochetLevel) {
        Objects.requireNonNull(start, "Start must be set to perform raycast");
        Objects.requireNonNull(end, "End must be set to perform raycast");
        if (ricochetLevel < 0) {
            // approximate infinite ricochet as 64 passes
            ricochetLevel = 64;
        }
        if (ricochetLevel > 64) {
            ricochetLevel = 64;
        }
        int castCount = 1 + ricochetLevel;

        // tracks the state of the live cast segment
        float rangeRemaining = Math.max(1.0f, (float) start.distanceTo(end));
        Vec3 castStart = start;
        Vec3 castEnd = end;

        List<HitResult> hitResults = new ArrayList<>();
        BlockHitResult lastBlockHitMiss = null;
        HashSet<UUID> hitEntities = new HashSet<>();
        ricochetCast:
        for (int i = 0; i < castCount && rangeRemaining > 0.5; i++) {
            if (checkForBlocks) {
                lastBlockHitMiss = level.clip(new ClipContext(castStart, castEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity == null ? CollisionContext.empty() : CollisionContext.of(originEntity)));
                castEnd = lastBlockHitMiss.getLocation();
            }
            AABB collider = new AABB(castStart, castEnd).inflate(2);
            List<? extends Entity> entities = level.getEntities(originEntity, collider, filter);
            boolean work = false;
            var distanceAnchor = castStart;
            entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(distanceAnchor)));
            for (Entity target : entities) {
                if (hitEntities.contains(target.getUUID())) {
                    continue;
                }
                HitResult hit = Utils.checkEntityIntersecting(target, castStart, castEnd, bbInflation);
                if (hit.getType() != HitResult.Type.MISS) {
                    work = true;
                    hitResults.add(hit);
                    if (i == castCount - 1) {
                        // we are at the final cast, no need to calculate future ricochet
                        break;
                    }
                    if (hit instanceof EntityHitResult entityHitResult) {
                        hitEntities.add(entityHitResult.getEntity().getUUID());
                    }
                    Vec3 direction = castEnd.subtract(castStart).normalize();
                    rangeRemaining -= (float) castStart.distanceTo(hit.getLocation());
                    castStart = hit.getLocation();
                    Optional<Vec3> ricochet = AbstractSkillProjectile.findRicochetDirection(level, castStart, direction, rangeRemaining, filter, hit instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : originEntity);
                    if (ricochet.isEmpty()) {
                        break ricochetCast;
                    }
                    castEnd = castStart.add(ricochet.get().scale(rangeRemaining));
                    break;
                }
            }
            if (!work) {
                break ricochetCast;
            }
        }
        if (hitResults.isEmpty()) {
            if (checkForBlocks) {
                assert lastBlockHitMiss != null; // loop is guaranteed to run, and block hit is guaranteed to be cast if checkForBlocks is set
                return List.of(lastBlockHitMiss);
            } else {
                return List.of(BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end)));
            }
        } else {
            return hitResults;
        }
    }

    /**
     * Performs the raycast with the current parameters. Start and end must be set.
     *
     * @return raycast HitResults
     */
    public List<HitResult> performRaycastWithPiercing(int pierceLevel) {
        Objects.requireNonNull(start, "Start must be set to perform raycast");
        Objects.requireNonNull(end, "End must be set to perform raycast");
        if (pierceLevel < 0) {
            // approximate infinite piercing as 64 passes
            pierceLevel = 64;
        }
        if (pierceLevel > 64) {
            pierceLevel = 64;
        }
        Vec3 castStart = start;
        Vec3 castEnd = end;
        List<HitResult> hitResults = new ArrayList<>();
        BlockHitResult endHit;
        if (checkForBlocks) {
            endHit = level.clip(new ClipContext(castStart, castEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity == null ? CollisionContext.empty() : CollisionContext.of(originEntity)));
            castEnd = endHit.getLocation();
        } else {
            endHit = BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
        }
        AABB collider = new AABB(castStart, castEnd).inflate(2);
        List<? extends Entity> entities = level.getEntities(originEntity, collider, filter);
        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(castStart)));
        boolean exhaustedPiercing = false;
        for (Entity target : entities) {
            HitResult hit = Utils.checkEntityIntersecting(target, castStart, castEnd, bbInflation);
            if (hit.getType() != HitResult.Type.MISS) {
                hitResults.add(hit);
                if (pierceLevel == 0) {
                    exhaustedPiercing = true;
                    break;
                }
                pierceLevel--;
            }
        }
        if (!exhaustedPiercing) {
            hitResults.add(endHit);
        }
        return hitResults;
    }
}
