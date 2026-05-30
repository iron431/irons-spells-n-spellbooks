package io.redspace.skillcasting.api.skill;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Outcome of a validation step. Either a success, or a failure carrying a player-facing message.
 */
public record CastResult(boolean success, @Nullable Component message) {
    private static final CastResult SUCCESS = new CastResult(true, null);

    public static CastResult isSuccess() {
        return SUCCESS;
    }

    public static CastResult failure(Component message) {
        return new CastResult(false, message);
    }

    public boolean isFailure() {
        return !success;
    }
}
