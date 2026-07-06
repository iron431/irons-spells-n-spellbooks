package io.redspace.skillcasting.api.resolver;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public interface PositionResolver {

    Type<? extends PositionResolver> type();

    Vec3 resolve(CastContext castContext, PositionAnchor anchor);

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
}
