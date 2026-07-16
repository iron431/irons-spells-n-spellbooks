package io.redspace.skillcasting.data.resolver;

import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record FixedDirectionResolver(Vec3 value) implements DirectionResolver {
    public static final StreamCodec<RegistryFriendlyByteBuf, FixedDirectionResolver> STREAM_CODEC =
            StreamCodec.of(
                    (buf, fixed) -> buf.writeVec3(fixed.value),
                    buf -> new FixedDirectionResolver(buf.readVec3()));

    @Override
    public Type<FixedDirectionResolver> type() {
        return SkillcastingResolverTypes.DIRECTION_FIXED.get();
    }

    @Override
    public Vec3 resolve(CastContext castContext) {
        return value;
    }

    public static FixedDirectionResolver of(Direction direction) {
        return new FixedDirectionResolver(Vec3.atLowerCornerOf(direction.getNormal()).normalize());
    }
}
