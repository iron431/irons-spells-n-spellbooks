package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record FixedPositionResolver(Vec3 value) implements PositionResolver {
    public static final StreamCodec<RegistryFriendlyByteBuf, FixedPositionResolver> STREAM_CODEC =
            StreamCodec.of(
                    (buf, fixed) -> buf.writeVec3(fixed.value),
                    buf -> new FixedPositionResolver(buf.readVec3()));

    @Override
    public Type<FixedPositionResolver> type() {
        return SkillcastingResolverTypes.POSITION_FIXED.get();
    }

    @Override
    public Vec3 resolve(CastContext castContext, PositionAnchor anchor) {
        return value;
    }
}
