package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import net.minecraft.world.phys.Vec3;

public record CasterDirectionResolver() implements DirectionResolver {
    public static final CasterDirectionResolver INSTANCE = new CasterDirectionResolver();

    @Override
    public Type<CasterDirectionResolver> type() {
        return SkillcastingResolverTypes.DIRECTION_CASTER.get();
    }

    @Override
    public Vec3 resolve(CastContext castContext) {
        return castContext.caster().forward();
    }
}
