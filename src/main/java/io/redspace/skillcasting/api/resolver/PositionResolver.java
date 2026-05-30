package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Resolves a cast origin in world space. {@link Type} variants are registered in
 * {@link io.redspace.skillcasting.registry.SkillcastingRegistries#POSITION_RESOLVER_TYPES}; runtime
 * values are installed on {@link io.redspace.skillcasting.registry.SkillcastingComponentTypes#POSITION_RESOLVER}.
 */
public interface PositionResolver {

    Type<? extends PositionResolver> type();

    Vec3 resolve(CastContext castContext, PositionAnchor anchor);

    /**
     * Registered resolver variant; owns the {@link StreamCodec} for instances of {@code T}.
     */
    record Type<T extends PositionResolver>(StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        @SuppressWarnings("unchecked")
        void encode(RegistryFriendlyByteBuf buf, PositionResolver resolver) {
            streamCodec.encode(buf, (T) resolver);
        }

        public PositionResolver decode(RegistryFriendlyByteBuf buf) {
            return streamCodec.decode(buf);
        }
    }

    static StreamCodec<RegistryFriendlyByteBuf, PositionResolver> codec(Registry<Type<?>> registry) {
        return StreamCodec.of(
                (buf, resolver) -> {
                    ResourceLocation id = Objects.requireNonNull(
                            registry.getKey(resolver.type()),
                            "Cannot dispatch unregistered position resolver type");
                    buf.writeResourceLocation(id);
                    resolver.type().encode(buf, resolver);
                },
                buf -> {
                    ResourceLocation id = buf.readResourceLocation();
                    Type<?> registered = registry.get(id);
                    if (registered == null) {
                        throw new IllegalStateException("Unknown position resolver type: " + id);
                    }
                    return registered.decode(buf);
                });
    }

    record Caster() implements PositionResolver {
        public static final Caster INSTANCE = new Caster();
        public static final StreamCodec<RegistryFriendlyByteBuf, Caster> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final Type<Caster> TYPE = new Type<>(STREAM_CODEC);

        @Override
        public Type<Caster> type() {
            return TYPE;
        }

        @Override
        public Vec3 resolve(CastContext castContext, PositionAnchor anchor) {
            return castContext.caster().position(anchor);
        }
    }

    record Fixed(Vec3 value) implements PositionResolver {
        public static final StreamCodec<RegistryFriendlyByteBuf, Fixed> STREAM_CODEC =
                StreamCodec.of(
                        (buf, fixed) -> buf.writeVec3(fixed.value),
                        buf -> new Fixed(buf.readVec3()));
        public static final Type<Fixed> TYPE = new Type<>(STREAM_CODEC);

        @Override
        public Type<Fixed> type() {
            return TYPE;
        }

        @Override
        public Vec3 resolve(CastContext castContext, PositionAnchor anchor) {
            return value;
        }
    }
}
