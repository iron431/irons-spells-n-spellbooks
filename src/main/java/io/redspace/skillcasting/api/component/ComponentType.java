package io.redspace.skillcasting.api.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Registry object that keys a typed slot on a {@link io.redspace.skillcasting.api.cast.CastContext}.
 * Optionally carries a persistence {@link Codec} and a {@link StreamCodec} for network sync.
 */
public class ComponentType<T> {
    @Nullable
    private final Codec<T> codec;
    @Nullable
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    protected ComponentType(@Nullable Codec<T> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public Optional<Codec<T>> codec() {
        return Optional.ofNullable(codec);
    }

    public Optional<StreamCodec<RegistryFriendlyByteBuf, T>> streamCodec() {
        return Optional.ofNullable(streamCodec);
    }

    public boolean isSynced() {
        return streamCodec != null;
    }

    public boolean isPersisted() {
        return codec != null;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        @Nullable
        private Codec<T> codec;
        @Nullable
        private StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Builder<T> persisted(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> synced(StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
            this.streamCodec = streamCodec;
            return this;
        }

        public ComponentType<T> build() {
            return new ComponentType<>(codec, streamCodec);
        }
    }
}
