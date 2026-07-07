package io.redspace.ironsspellbooks.api.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AnimationHolder {
    public enum Type {
        PASS,
        STOP,
        ANIMATION
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Type> TYPE_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> Type.values()[i], Type::ordinal), t -> t, t -> t);

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationHolder> STREAM_CODEC = StreamCodec.of(
            (buf, holder) -> {
                TYPE_CODEC.encode(buf, holder.type);
                if (holder.type == Type.ANIMATION) {
                    buf.writeResourceLocation(holder.getAnimationResource().orElseThrow());
                    buf.writeBoolean(holder.animatesLegs);
                }
            },
            buf -> switch (TYPE_CODEC.decode(buf)) {
                case PASS -> pass();
                case STOP -> stop();
                case ANIMATION -> new AnimationHolder(buf.readResourceLocation(), buf.readBoolean());
            });

    private static final AnimationHolder STOP_INSTANCE = new AnimationHolder(Type.STOP);
    private static final AnimationHolder PASS_INSTANCE = new AnimationHolder(Type.PASS);

    // todo: also include an animation source resource for geckolib? no sure how that works at the moment
    private final @Nullable ResourceLocation animation;

    private final Type type;
    private final boolean animatesLegs;

    public AnimationHolder(@NotNull ResourceLocation animation, boolean animatesLegs) {
        // todo: overload w/ legs: false
        this.animation = animation;
        this.type = Type.ANIMATION;
        this.animatesLegs = animatesLegs;
    }

    private AnimationHolder(Type type) {
        this.animation = null;
        this.type = type;
        this.animatesLegs = false;
    }

    public Type getType() {
        return type;
    }

    public boolean isAnimatesLegs() {
        return animatesLegs;
    }

    public Optional<ResourceLocation> getAnimationResource() {
        return Optional.ofNullable(animation);
    }

    public static AnimationHolder stop() {
        return STOP_INSTANCE;
    }

    public static AnimationHolder pass() {
        return PASS_INSTANCE;
    }
}