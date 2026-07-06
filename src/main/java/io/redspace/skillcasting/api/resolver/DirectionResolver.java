package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.cast.CastContext;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public interface DirectionResolver {
    Type<? extends DirectionResolver> type();

    Vec3 resolve(CastContext castContext);

    static FixedDirectionResolver fixed(Vec3 vec3) {
        return new FixedDirectionResolver(vec3);
    }

    static FixedDirectionResolver fixedFromRotation(Vec2 rotation) {
        return new FixedDirectionResolver(Vec3.directionFromRotation(rotation));
    }

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

}
