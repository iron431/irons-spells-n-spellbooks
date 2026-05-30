package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.cast.CastContext;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Resolves a cast facing in world space. {@link Type} variants are registered in
 * {@link io.redspace.skillcasting.registry.SkillcastingRegistries#DIRECTION_RESOLVER_TYPES}; runtime
 * values are installed on {@link io.redspace.skillcasting.registry.SkillcastingComponentTypes#DIRECTION_RESOLVER}.
 */
public interface DirectionResolver {
    Type<? extends DirectionResolver> type();

    Vec3 resolve(CastContext ctx);

    static Fixed fixed(Vec3 vec3) {
        return new Fixed(vec3);
    }

    static Fixed fixedFromRotation(Vec2 rotation) {
        return new Fixed(Vec3.directionFromRotation(rotation));
    }

    static Caster caster() {
        return Caster.INSTANCE;
    }

    /**
     * Registered resolver variant; owns the {@link StreamCodec} for instances of {@code T}.
     */
    record Type<T extends DirectionResolver>(StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        @SuppressWarnings("unchecked")
        void encode(RegistryFriendlyByteBuf buf, DirectionResolver resolver) {
            streamCodec.encode(buf, (T) resolver);
        }

        public DirectionResolver decode(RegistryFriendlyByteBuf buf) {
            return streamCodec.decode(buf);
        }
    }

    static StreamCodec<RegistryFriendlyByteBuf, DirectionResolver> codec(Registry<Type<?>> registry) {
        return StreamCodec.of(
                (buf, resolver) -> {
                    ResourceLocation id = Objects.requireNonNull(
                            registry.getKey(resolver.type()),
                            "Cannot dispatch unregistered direction resolver type");
                    buf.writeResourceLocation(id);
                    resolver.type().encode(buf, resolver);
                },
                buf -> {
                    ResourceLocation id = buf.readResourceLocation();
                    Type<?> registered = registry.get(id);
                    if (registered == null) {
                        throw new IllegalStateException("Unknown direction resolver type: " + id);
                    }
                    return registered.decode(buf);
                });
    }

    record Caster() implements DirectionResolver {
        public static final Caster INSTANCE = new Caster();
        public static final StreamCodec<RegistryFriendlyByteBuf, Caster> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final Type<Caster> TYPE = new Type<>(STREAM_CODEC);

        @Override
        public Type<Caster> type() {
            return TYPE;
        }

        @Override
        public Vec3 resolve(CastContext ctx) {
            return ctx.caster().forward();
        }
    }

    record Fixed(Vec3 value) implements DirectionResolver {
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
        public Vec3 resolve(CastContext ctx) {
            return value;
        }

        /** Unit vector along a block facing. */
        public static Fixed of(Direction direction) {
            return new Fixed(Vec3.atLowerCornerOf(direction.getNormal()).normalize());
        }
    }
}
