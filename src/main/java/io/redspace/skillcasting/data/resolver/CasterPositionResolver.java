package io.redspace.skillcasting.data.resolver;

import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import net.minecraft.world.phys.Vec3;

public record CasterPositionResolver() implements PositionResolver {
    public static final CasterPositionResolver INSTANCE = new CasterPositionResolver();

    @Override
    public Type<CasterPositionResolver> type() {
        return SkillcastingResolverTypes.POSITION_CASTER.get();
    }

    @Override
    public Vec3 resolve(CastContext castContext, PositionAnchor anchor) {
        return castContext.caster().position(anchor);
    }
}
