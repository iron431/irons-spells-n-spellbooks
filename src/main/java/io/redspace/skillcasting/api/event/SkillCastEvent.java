package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastResult;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class SkillCastEvent extends SkillEvent {
    protected SkillCastEvent(CastContext castContext) {
        super(castContext.skill(), castContext);
    }

    public static class Check extends SkillCastEvent {
        private CastResult result;

        public Check(CastContext castContext, CastResult result) {
            super(castContext);
            this.result = Objects.requireNonNull(result);
        }

        public CastResult getResult() {
            return result;
        }

        public void setResult(CastResult result) {
            this.result = Objects.requireNonNull(result);
        }
    }

    public static class PreCast extends SkillCastEvent implements ICancellableEvent {
        @Nullable
        private Component cancelMessage;

        public PreCast(CastContext castContext) {
            super(castContext);
        }

        public void cancelWithMessage(Component message) {
            this.cancelMessage = message;
            setCanceled(true);
        }

        @Nullable
        public Component getCancelMessage() {
            return cancelMessage;
        }
    }

    public static class PostCast extends SkillCastEvent {
        public PostCast(CastContext castContext) {
            super(castContext);
        }
    }

    public static class Complete extends SkillCastEvent {
        protected final CastEndReason castEndReason;

        public Complete(CastContext castContext, CastEndReason castEndReason) {
            super(castContext);
            this.castEndReason = castEndReason;
        }

        public CastEndReason getCastEndReason() {
            return castEndReason;
        }
    }
}
