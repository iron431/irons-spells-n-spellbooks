package io.redspace.skillcasting;

import net.minecraft.world.level.Level;

/**
 * Shared game-time helpers for skillcasting timers. All deadlines use {@link Level#getGameTime()}.
 */
@Deprecated
public final class SkillcastingTime {
    private SkillcastingTime() {
    }

    public static long gameTime(Level level) {
        return level.getGameTime();
    }

    public static long endsAt(long gameTime, int durationTicks) {
        return gameTime + durationTicks;
    }

    public static int remainingTicks(long gameTime, long endsAtGameTime) {
        return (int) Math.max(0, endsAtGameTime - gameTime);
    }

    public static boolean isExpired(long gameTime, long endsAtGameTime) {
        return gameTime >= endsAtGameTime;
    }
}
