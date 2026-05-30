package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration for built-in {@link PositionResolver.Type} and {@link DirectionResolver.Type} variants.
 */
public final class SkillcastingResolverTypes {
    private static final DeferredRegister<PositionResolver.Type<?>> POSITION_RESOLVER_TYPES =
            DeferredRegister.create(SkillcastingRegistries.POSITION_RESOLVER_TYPE_KEY, Skillcasting.NAMESPACE);

    private static final DeferredRegister<DirectionResolver.Type<?>> DIRECTION_RESOLVER_TYPES =
            DeferredRegister.create(SkillcastingRegistries.DIRECTION_RESOLVER_TYPE_KEY, Skillcasting.NAMESPACE);

    public static final DeferredHolder<PositionResolver.Type<?>, PositionResolver.Type<PositionResolver.Caster>> POSITION_CASTER =
            POSITION_RESOLVER_TYPES.register("caster", () -> PositionResolver.Caster.TYPE);

    public static final DeferredHolder<PositionResolver.Type<?>, PositionResolver.Type<PositionResolver.Fixed>> POSITION_FIXED =
            POSITION_RESOLVER_TYPES.register("fixed", () -> PositionResolver.Fixed.TYPE);

    public static final DeferredHolder<DirectionResolver.Type<?>, DirectionResolver.Type<DirectionResolver.Caster>> DIRECTION_CASTER =
            DIRECTION_RESOLVER_TYPES.register("caster", () -> DirectionResolver.Caster.TYPE);

    public static final DeferredHolder<DirectionResolver.Type<?>, DirectionResolver.Type<DirectionResolver.Fixed>> DIRECTION_FIXED =
            DIRECTION_RESOLVER_TYPES.register("fixed", () -> DirectionResolver.Fixed.TYPE);

    private SkillcastingResolverTypes() {
    }

    public static void register(IEventBus eventBus) {
        POSITION_RESOLVER_TYPES.register(eventBus);
        DIRECTION_RESOLVER_TYPES.register(eventBus);
    }
}
