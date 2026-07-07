package io.redspace.skillcasting.api.skill;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Data object representing whether a cast can proceed.
 * @param isSuccess whether the cast can successfully proceed.
 * @param message (Nullable) actionbar message to send if the caster is a player; sent on success or failure if set.
 */
public record CastResult(boolean isSuccess, @Nullable Component message) {
    private static final CastResult SUCCESS = new CastResult(true, null);

    public static CastResult success() {
        return SUCCESS;
    }

    public static CastResult failure(Component message) {
        return new CastResult(false, message);
    }

    public boolean isFailure() {
        return !isSuccess;
    }
}
