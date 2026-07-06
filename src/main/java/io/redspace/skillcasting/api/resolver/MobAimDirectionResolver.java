package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.network.ComponentSyncCodecs;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MobAimDirectionResolver implements DirectionResolver {
    public static final StreamCodec<RegistryFriendlyByteBuf, MobAimDirectionResolver> STREAM_CODEC = StreamCodec.composite(
            ComponentSyncCodecs.VEC3, MobAimDirectionResolver::getAimPosition,
            MobAimDirectionResolver::new);

    private Vec3 aimPosition;

    public MobAimDirectionResolver(CastContext castContext) {
        aimPosition = castContext.position(PositionAnchor.CASTING_POSITION).add(castContext.direction());
    }

    private MobAimDirectionResolver(Vec3 aimPosition) {
        this.aimPosition = aimPosition;
    }

    @Override
    public Type<MobAimDirectionResolver> type() {
        return SkillcastingResolverTypes.DIRECTION_MOB_AIM.get();
    }

    @Override
    public Vec3 resolve(CastContext castContext) {
        return aimPosition.subtract(castContext.position(PositionAnchor.CASTING_POSITION)).normalize();
    }

    public void updateAim(CastContext castContext, @Nullable Entity target, float strength) {
        if (target == null) {
            return;
        }
        Vec3 wanted = target.getBoundingBox().getCenter();
        if (aimPosition.equals(Vec3.ZERO)) {
            aimPosition = wanted;
        } else {
            aimPosition = aimPosition.add(wanted.subtract(aimPosition).scale(strength));
        }
        castContext.components().markSyncedDirty(SkillcastingComponentTypes.DIRECTION_RESOLVER.get());
    }

    public Vec3 getAimPosition() {
        return aimPosition;
    }
}
