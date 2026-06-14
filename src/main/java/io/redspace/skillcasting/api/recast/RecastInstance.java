package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.cast.CastContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class RecastInstance {
    public static final Codec<RecastInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("total_casts").forGetter(inst -> inst.config.totalCasts()),
            Codec.INT.fieldOf("duration").forGetter(inst -> inst.config.durationTicks()),
            Codec.INT.fieldOf("remaining_casts").forGetter(RecastInstance::remainingCasts),
            Codec.INT.fieldOf("ticks_remaining").forGetter(RecastInstance::ticksRemaining),
            CastComponentMap.COMPONENT_CODEC.fieldOf("components").forGetter(RecastInstance::components)
    ).apply(builder, RecastInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecastInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, inst -> inst.config.totalCasts(),
            ByteBufCodecs.INT, inst -> inst.config.durationTicks(),
            ByteBufCodecs.INT, RecastInstance::remainingCasts,
            ByteBufCodecs.VAR_INT, RecastInstance::ticksRemaining,
            CastComponentMap.STREAM_CODEC, RecastInstance::components,
            RecastInstance::new
    );

    private final RecastConfig config;
    private int remainingCasts;
    private int ticksRemaining;

    private final CastComponentMap components;

    public RecastInstance(RecastConfig config, CastContext castContext) {
        this(config, config.totalCasts() - 1, config.durationTicks(), castContext);
    }

    private RecastInstance(RecastConfig config, int remainingCasts, int windowTicksRemaining, CastContext castContext) {
        this.config = config;
        this.remainingCasts = remainingCasts;
        this.ticksRemaining = windowTicksRemaining;
        this.components = castContext.components();
    }

    private RecastInstance(int total, int duration, int remainingCasts, int windowTicksRemaining, CastComponentMap snapshot) {
        this.config = new RecastConfig(total, duration);
        this.remainingCasts = remainingCasts;
        this.ticksRemaining = windowTicksRemaining;
        this.components = snapshot;
    }

    public RecastConfig config() {
        return config;
    }

    public int remainingCasts() {
        return remainingCasts;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    public CastComponentMap components() {
        return components;
    }

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public void consumeCast() {
        if (remainingCasts > 0) {
            remainingCasts--;
        }
        ticksRemaining = config.durationTicks();
    }

    public boolean isTimedOut() {
        return ticksRemaining <= 0;
    }

    public boolean usedAllCasts() {
        return remainingCasts <= 0;
    }
}
