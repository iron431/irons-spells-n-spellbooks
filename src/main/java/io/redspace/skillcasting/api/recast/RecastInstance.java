package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.SkillcastingTime;
import io.redspace.skillcasting.api.cast.CastContext;

public final class RecastInstance {
    // fixme: null cast context. need cast context serialization.
    public static final Codec<RecastInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("total_casts").forGetter(inst -> inst.config.totalCasts()),
            Codec.INT.fieldOf("duration").forGetter(inst -> inst.config.durationTicks()),
            Codec.INT.fieldOf("remaining_casts").forGetter(inst -> inst.remainingCasts),
            Codec.LONG.fieldOf("end_game_time").forGetter(inst -> inst.windowEndsAtGameTime)
    ).apply(builder, (total, duration, remaining, end) -> new RecastInstance(new RecastConfig(total, duration), remaining, end, null)));

    private final RecastConfig config;
    private int remainingCasts;
    private long windowEndsAtGameTime;
    private final CastContext castContext;

    public RecastInstance(RecastConfig config, CastContext castContext) {
        this(config, config.totalCasts() - 1, SkillcastingTime.endsAt(castContext.level().getGameTime(), config.durationTicks()), castContext);
    }

    public RecastInstance(RecastConfig config, int remainingCasts, long windowEndsAtGameTime, CastContext castContext) {
        this.config = config;
        this.remainingCasts = remainingCasts;
        this.windowEndsAtGameTime = windowEndsAtGameTime;
        this.castContext = castContext;
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

    public CastContext castContext() {
        return castContext;
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
