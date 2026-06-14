package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.SkillcastingTime;
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
            Codec.LONG.fieldOf("end_game_time").forGetter(RecastInstance::windowEndsAtGameTime),
            CastComponentMap.COMPONENT_CODEC.fieldOf("components").forGetter(RecastInstance::components)
    ).apply(builder, RecastInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecastInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, inst -> inst.config.totalCasts(),
            ByteBufCodecs.INT, inst -> inst.config.durationTicks(),
            ByteBufCodecs.INT, RecastInstance::remainingCasts,
            ByteBufCodecs.VAR_LONG, RecastInstance::windowEndsAtGameTime,
            CastComponentMap.STREAM_CODEC, RecastInstance::components,
            RecastInstance::new
    );

    private final RecastConfig config;
    private int remainingCasts;
    private long windowEndsAtGameTime;

    public CastComponentMap components() {
        return components;
    }

    private final CastComponentMap components;

    public RecastInstance(RecastConfig config, CastContext castContext) {
        this(config, config.totalCasts() - 1, SkillcastingTime.endsAt(castContext.level().getGameTime(), config.durationTicks()), castContext);
    }

    private RecastInstance(RecastConfig config, int remainingCasts, long windowEndsAtGameTime, CastContext castContext) {
        this.config = config;
        this.remainingCasts = remainingCasts;
        this.windowEndsAtGameTime = windowEndsAtGameTime;
        this.components = castContext.components();
    }

    private RecastInstance(int total, int duration, int remainingCasts, long windowEndsAtGameTime, CastComponentMap snapshot) {
        this.config = new RecastConfig(total, duration);
        this.remainingCasts = remainingCasts;
        this.windowEndsAtGameTime = windowEndsAtGameTime;
        this.components = snapshot;
    }

    public RecastConfig config() {
        return config;
    }

    public int remainingCasts() {
        return remainingCasts;
    }

    public long windowEndsAtGameTime() {
        return windowEndsAtGameTime;
    }

    public int ticksRemaining(long gameTime) {
        return SkillcastingTime.remainingTicks(gameTime, windowEndsAtGameTime);
    }

    public void consumeCast(long gametime) {
        if (remainingCasts > 0) {
            remainingCasts--;
        }
        this.windowEndsAtGameTime = gametime + config.durationTicks();
    }

    public boolean isTimedOut(long gameTime) {
        return SkillcastingTime.isExpired(gameTime, windowEndsAtGameTime);
    }

    public boolean exhausted() {
        return remainingCasts <= 0;
    }
}
