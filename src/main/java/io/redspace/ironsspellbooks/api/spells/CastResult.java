package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CastResult {
    public enum Type {
        SUCCESS,
        FAILURE
    }

    public final Type type;
    public final @Nullable Component message;

    public CastResult(Type type) {
        this(type, null);
    }

    public CastResult(Type type, Component message) {
        this.type = type;
        this.message = message;
    }

    public boolean isSuccess() {
        return this.type == Type.SUCCESS;
    }
}
