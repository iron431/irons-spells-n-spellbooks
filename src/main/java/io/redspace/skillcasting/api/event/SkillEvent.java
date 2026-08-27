package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.recast.RecastResult;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class SkillEvent extends Event {
    protected final Holder<AbstractSkill> skill;
    protected final CastContext castContext;

    protected SkillEvent(Holder<AbstractSkill> skill, CastContext castContext) {
        this.skill = skill;
        this.castContext = castContext;
    }

    public CastContext getCastContext() {
        return castContext;
    }

    public Holder<AbstractSkill> getSkill() {
        return skill;
    }

    public static class BeforeCastStart extends SkillEvent implements ICancellableEvent {
        public BeforeCastStart(CastContext castContext) {
            super(castContext.skill(), castContext);
        }
    }

    public static class OnCast extends SkillEvent {
        public OnCast(CastContext castContext) {
            super(castContext.skill(), castContext);
        }
    }

    public static class OnCastComplete extends SkillEvent {
        protected final CastEndReason castEndReason;

        public OnCastComplete(CastContext castContext, CastEndReason castEndReason) {
            super(castContext.skill(), castContext);
            this.castEndReason = castEndReason;
        }

        public CastEndReason getCastEndReason() {
            return castEndReason;
        }
    }

    public static class OnRecastStart extends SkillEvent implements ICancellableEvent {
        protected final RecastInstance recastInstance;

        public OnRecastStart(CastContext castContext, RecastInstance recastInstance) {
            super(castContext.skill(), castContext);
            this.recastInstance = recastInstance;
        }

        public RecastInstance getRecastInstance() {
            return recastInstance;
        }

        public void setRemainingCasts(int remainingCasts) {
            recastInstance.setRemainingCasts(remainingCasts);
        }

        public void setWindowDuration(int durationTicks) {
            recastInstance.setWindowDuration(durationTicks);
        }
    }

    public static class OnRecastComplete extends SkillEvent {
        protected final RecastInstance recastInstance;
        protected final RecastResult recastResult;

        public OnRecastComplete(CastContext castContext, RecastInstance recastInstance, RecastResult recastResult) {
            super(castContext.skill(), castContext);
            this.recastInstance = recastInstance;
            this.recastResult = recastResult;
        }
        public RecastInstance getRecastInstance() {
            return recastInstance;
        }
        public RecastResult getRecastResult() {
            return recastResult;
        }
    }
}
