package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.SkillcastingTime;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RecastInstance {
    public static final Codec<RecastInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("total_casts").forGetter(inst -> inst.config.totalCasts()),
            Codec.INT.fieldOf("duration").forGetter(inst -> inst.config.durationTicks()),
            Codec.INT.fieldOf("remaining_casts").forGetter(RecastInstance::remainingCasts),
            Codec.LONG.fieldOf("end_game_time").forGetter(RecastInstance::windowEndsAtGameTime),
            CastContext.SNAPSHOT_CODEC.fieldOf("cast_context").forGetter(RecastInstance::castContextSnapshot)
    ).apply(builder, RecastInstance::fromSnapshot));

    private final RecastConfig config;
    private int remainingCasts;
    private long windowEndsAtGameTime;
    @Nullable
    private CastContext castContext;
    @Nullable
    private CastContext.Snapshot pendingSnapshot;

    public RecastInstance(RecastConfig config, CastContext castContext) {
        this(config, config.totalCasts() - 1, SkillcastingTime.endsAt(castContext.level().getGameTime(), config.durationTicks()), castContext);
    }

    public RecastInstance(RecastConfig config, int remainingCasts, long windowEndsAtGameTime, CastContext castContext) {
        this.config = config;
        this.remainingCasts = remainingCasts;
        this.windowEndsAtGameTime = windowEndsAtGameTime;
        this.castContext = castContext;
        this.pendingSnapshot = null;
    }

    private RecastInstance(RecastConfig config, int remainingCasts, long windowEndsAtGameTime, CastContext.Snapshot snapshot) {
        this.config = config;
        this.remainingCasts = remainingCasts;
        this.windowEndsAtGameTime = windowEndsAtGameTime;
        this.castContext = null;
        this.pendingSnapshot = snapshot;
    }

    private static RecastInstance fromSnapshot(
            int totalCasts,
            int duration,
            int remainingCasts,
            long windowEndsAtGameTime,
            CastContext.Snapshot snapshot) {
        return new RecastInstance(new RecastConfig(totalCasts, duration), remainingCasts, windowEndsAtGameTime, snapshot);
    }

    public static RecastInstance fromSnapshot(
            RecastConfig config,
            int remainingCasts,
            long windowEndsAtGameTime,
            CastContext.Snapshot snapshot) {
        return new RecastInstance(config, remainingCasts, windowEndsAtGameTime, snapshot);
    }

    public void rehydrate(CasterRef caster) {
        if (pendingSnapshot != null) {
            castContext = pendingSnapshot.restoreFrom(caster);
            pendingSnapshot = null;
        }
    }

    public boolean needsRehydration() {
        return pendingSnapshot != null;
    }

    private CastContext.Snapshot castContextSnapshot() {
        if (castContext != null) {
            return castContext.toSnapshot();
        }
        return Objects.requireNonNull(pendingSnapshot, "Recast has no cast context snapshot");
    }

    public Map<ComponentType<?>, Object> syncedComponentsForNetwork() {
        if (castContext != null) {
            return castContext.getAllSynced();
        }
        Map<ComponentType<?>, Object> synced = new HashMap<>();
        for (Map.Entry<ComponentType<?>, Object> entry : Objects.requireNonNull(pendingSnapshot).components().entrySet()) {
            if (entry.getKey().isSynced()) {
                synced.put(entry.getKey(), entry.getValue());
            }
        }
        return synced;
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
        if (castContext == null) {
            throw new IllegalStateException("Recast cast context is not rehydrated yet");
        }
        return castContext;
    }

    @Nullable
    public CastContext castContextOrNull() {
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
