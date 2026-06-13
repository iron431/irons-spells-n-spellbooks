package io.redspace.skillcasting.lifecycle;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.level.Level;

public class ActiveCast {
    private final CastContext context;
    private final long startedAtGameTime;

    public ActiveCast(CastContext context, long gameTime) {
        this.context = context;
        this.startedAtGameTime = gameTime;
    }

    public CastContext context() {
        return context;
    }

    public long startedAtGameTime() {
        return startedAtGameTime;
    }

    public int durationTicks() {
        return context.get(SkillcastingComponentTypes.CAST_TIME.get());
    }

    public int elapsedTicks(long gameTime) {
        return (int) Math.max(0, gameTime - startedAtGameTime);
    }

    public int remainingTicks(long gameTime) {
        return Math.max(0, durationTicks() - elapsedTicks(gameTime));
    }

    public float completionPercent(long gameTime) {
        int duration = durationTicks();
        if (duration <= 0) {
            return 0;
        }
        return Math.min(1f, elapsedTicks(gameTime) / (float) duration);
    }

    public Level level() {
        return context.level();
    }
}
