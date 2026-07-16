package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.resolver.CasterDirectionResolver;
import io.redspace.skillcasting.data.resolver.CasterPositionResolver;
import io.redspace.skillcasting.data.resolver.DirectionResolver;
import io.redspace.skillcasting.data.resolver.FixedDirectionResolver;
import io.redspace.skillcasting.data.resolver.FixedPositionResolver;
import io.redspace.skillcasting.data.resolver.MobAimDirectionResolver;
import io.redspace.skillcasting.data.resolver.PositionResolver;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SkillcastingResolverTypes {
    private static final DeferredRegister<PositionResolver.Type<?>> POSITION_RESOLVER_TYPES =
            DeferredRegister.create(SkillcastingRegistries.POSITION_RESOLVER_TYPE_KEY, Skillcasting.NAMESPACE);

    private static final DeferredRegister<DirectionResolver.Type<?>> DIRECTION_RESOLVER_TYPES =
            DeferredRegister.create(SkillcastingRegistries.DIRECTION_RESOLVER_TYPE_KEY, Skillcasting.NAMESPACE);

    public static final DeferredHolder<PositionResolver.Type<?>, PositionResolver.Type<CasterPositionResolver>> POSITION_CASTER =
            POSITION_RESOLVER_TYPES.register("caster", () -> new PositionResolver.Type<>(StreamCodec.unit(CasterPositionResolver.INSTANCE)));

    public static final DeferredHolder<PositionResolver.Type<?>, PositionResolver.Type<FixedPositionResolver>> POSITION_FIXED =
            POSITION_RESOLVER_TYPES.register("fixed", () -> new PositionResolver.Type<>(FixedPositionResolver.STREAM_CODEC));

    public static final DeferredHolder<DirectionResolver.Type<?>, DirectionResolver.Type<CasterDirectionResolver>> DIRECTION_CASTER =
            DIRECTION_RESOLVER_TYPES.register("caster", () -> new DirectionResolver.Type<>(StreamCodec.unit(CasterDirectionResolver.INSTANCE)));

    public static final DeferredHolder<DirectionResolver.Type<?>, DirectionResolver.Type<FixedDirectionResolver>> DIRECTION_FIXED =
            DIRECTION_RESOLVER_TYPES.register("fixed", () -> new DirectionResolver.Type<>(FixedDirectionResolver.STREAM_CODEC));

    public static final DeferredHolder<DirectionResolver.Type<?>, DirectionResolver.Type<MobAimDirectionResolver>> DIRECTION_MOB_AIM =
            DIRECTION_RESOLVER_TYPES.register("mob_aim", () -> new DirectionResolver.Type<>(MobAimDirectionResolver.STREAM_CODEC));

    private SkillcastingResolverTypes() {
    }

    public static void register(IEventBus eventBus) {
        POSITION_RESOLVER_TYPES.register(eventBus);
        DIRECTION_RESOLVER_TYPES.register(eventBus);
    }
}
