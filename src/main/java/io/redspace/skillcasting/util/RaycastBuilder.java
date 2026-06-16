package io.redspace.skillcasting.util;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.EntityCasterRef;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class RaycastBuilder {

    private final Level level;
    private final @Nullable Entity originEntity;
    private Vec3 start;
    private Vec3 end;
    private boolean checkForBlocks = false;
    private float bbInflation = 0;
    private Predicate<? super Entity> filter = Utils::canHitWithRaycast;

    public RaycastBuilder(Level level, @Nullable Entity originEntity) {
        this.level = level;
        this.originEntity = originEntity;
    }

    public static RaycastBuilder begin(Level level, @Nullable Entity originEntity) {
        return new RaycastBuilder(level, originEntity);
    }

    public static RaycastBuilder fromCast(CastContext castContext, PositionAnchor anchor, float range) {
        return RaycastBuilder.begin(castContext.level(), castContext.caster() instanceof EntityCasterRef entity ? entity.entity() : null)
                .start(castContext.position(anchor))
                .end(castContext.direction(), range);
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

    public RaycastBuilder filter(Predicate<? super Entity> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Executes the raycast with the current parameters. Start and end must have been set.
     *
     * @return the hit result (entity hit, block hit, or miss)
     */
    public HitResult build() {
        return performRaycast();
    }

    /**
     * Performs the raycast with the current parameters. Start and end must have been set.
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
}
