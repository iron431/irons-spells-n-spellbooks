package io.redspace.skillcasting.data.cast;

import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.level.Level;

public class ActiveCast {
    private final CastContext context;
    private final long startedAtGameTime;

    public ActiveCast(CastContext context) {
        this.context = context;
        this.startedAtGameTime = context.level().getGameTime();
    }

    public CastContext context() {
        return context;
    }

    public long startedAtGameTime() {
        return startedAtGameTime;
    }

    public int durationTicks() {
        return context.getOrDefault(SkillcastingComponentTypes.CAST_TIME, context.skill().value().getCastTimeTicks());
    }

    public int elapsedTicks(long gameTime) {
        return (int) Math.max(0, gameTime - startedAtGameTime);
    }

    public int remainingTicks(long gameTime) {
        return Math.max(0, durationTicks() - elapsedTicks(gameTime));
    }

    public float completionPercent(long gameTime) {
        return completionPercent(gameTime, 0);
    }

    public float completionPercent(long gameTime, float partialTick) {
        int duration = durationTicks();
        if (duration <= 0) {
            return 0;
        }
        return Math.min(1f, (elapsedTicks(gameTime) + partialTick) / (float) duration);
    }

    public Level level() {
        return context.level();
    }
}
