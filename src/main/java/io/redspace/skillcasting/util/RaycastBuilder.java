package io.redspace.skillcasting.util;

import io.redspace.skillcasting.data.skill.AbstractSkillProjectile;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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
    private Predicate<Entity> filter = SkillcastingUtils::canHitWithRaycast;

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
            HitResult hit = SkillcastingUtils.checkEntityIntersecting(target, start, rayEnd, bbInflation);
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

    public List<HitResult> performRaycastWithPiercing(int pierceLevel) {
        return performRaycastWithPiercingAndRicochet(pierceLevel, 0, false);
    }

    public List<HitResult> performRaycastWithPiercingAndRicochet(CastContext castContext, boolean allowBlockRicochet) {
        return performRaycastWithPiercingAndRicochet(
                castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_PIERCE, 0),
                castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_RICOCHET, 0),
                allowBlockRicochet
        );
    }

    /**
     * Performs the raycast with the current parameters, combining piercing and ricochet in a single
     * optimized solver. Start and end must be set.
     * <p>
     * Attempts to consume a ricochet level to redirect. If no charges or no valid direction is found, it will attempt to consume piercing charges until a final entity is hit.
     * Each entity hit attempts to ricochet again.
     *
     * @param allowBlockRicochet whether striking a block surface reflects the beam (consuming a ricochet
     *                           charge) instead of terminating at the wall.
     * @return raycast HitResults, ordered from origin outward along the beam path
     */
    public List<HitResult> performRaycastWithPiercingAndRicochet(int pierceLevel, int ricochetLevel, boolean allowBlockRicochet) {
        Objects.requireNonNull(start, "Start must be set to perform raycast");
        Objects.requireNonNull(end, "End must be set to perform raycast");

        // negative levels approximate "infinite"; everything is clamped to a sane upper bound
        if (pierceLevel < 0 || pierceLevel > 64) {
            pierceLevel = 64;
        }
        if (ricochetLevel < 0 || ricochetLevel > 64) {
            ricochetLevel = 64;
        }

        // live cast state and remaining budgets
        float rangeRemaining = Math.max(1.0f, (float) start.distanceTo(end));
        Vec3 castStart = start;
        Vec3 castEnd = end;
        int pierceRemaining = pierceLevel;
        int ricochetRemaining = ricochetLevel;

        List<HitResult> hitResults = new ArrayList<>();
        HashSet<UUID> hitEntities = new HashSet<>();

        // each bounce (entity ricochet or block reflection) consumes a ricochet charge and starts a new
        // segment, so the chain is bounded by the ricochet budget plus the initial segment
        for (int segment = 0; segment <= ricochetLevel && rangeRemaining > 0.5; segment++) {
            // clamp the segment to the first block surface, or fabricate a miss at the segment end
            BlockHitResult blockHit;
            if (checkForBlocks) {
                blockHit = level.clip(new ClipContext(castStart, castEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity == null ? CollisionContext.empty() : CollisionContext.of(originEntity)));
                castEnd = blockHit.getLocation();
            } else {
                blockHit = BlockHitResult.miss(castEnd, Direction.UP, BlockPos.containing(castEnd));
            }
            boolean hitWall = checkForBlocks && blockHit.getType() != HitResult.Type.MISS;

            AABB collider = new AABB(castStart, castEnd).inflate(2);
            List<? extends Entity> entities = level.getEntities(originEntity, collider, filter);
            final Vec3 sortAnchor = castStart;
            entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(sortAnchor)));
            Vec3 direction = castEnd.subtract(castStart).normalize();

            // walk the entities intersected along this straight segment
            boolean redirected = false;
            for (Entity target : entities) {
                if (hitEntities.contains(target.getUUID())) {
                    continue;
                }
                HitResult hit = SkillcastingUtils.checkEntityIntersecting(target, castStart, castEnd, bbInflation);
                if (hit.getType() == HitResult.Type.MISS) {
                    continue;
                }
                hitResults.add(hit);
                hitEntities.add(target.getUUID());

                // ricochet takes priority: consume a bounce charge if a valid redirect exists
                if (ricochetRemaining > 0) {
                    float bounceRange = rangeRemaining - (float) castStart.distanceTo(hit.getLocation());
                    Optional<Vec3> ricochet = AbstractSkillProjectile.findRicochetDirection(level, hit.getLocation(), direction, bounceRange, filter, target);
                    if (ricochet.isPresent()) {
                        ricochetRemaining--;
                        rangeRemaining = bounceRange;
                        castStart = hit.getLocation();
                        castEnd = castStart.add(ricochet.get().scale(rangeRemaining));
                        redirected = true;
                        break;
                    }
                }

                // no valid bounce: pierce through and keep traveling in the active direction
                if (pierceRemaining > 0) {
                    pierceRemaining--;
                    continue;
                }

                // out of both ricochet and piercing budget: the beam stops at this entity
                return hitResults;
            }

            if (redirected) {
                continue;
            }

            // the segment passed every entity available; resolve how it ends
            if (hitWall) {
                hitResults.add(blockHit);
                if (allowBlockRicochet && ricochetRemaining > 0) {
                    // reflect off the surface normal and continue into a new segment
                    ricochetRemaining--;
                    rangeRemaining -= (float) castStart.distanceTo(blockHit.getLocation());
                    castStart = blockHit.getLocation();
                    Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
                    Vec3 reflected = direction.subtract(normal.scale(2 * normal.dot(direction)));
                    castEnd = castStart.add(reflected.scale(rangeRemaining));
                    continue;
                }
                return hitResults;
            }

            // reached full range with nothing left to hit; cap the chain with a terminal endpoint
            hitResults.add(blockHit);
            return hitResults;
        }

        if (hitResults.isEmpty()) {
            // defensive fallback; the loop normally adds at least one terminal result above
            return List.of(BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end)));
        }
        return hitResults;
    }
}
