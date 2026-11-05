package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.recast.RecastResult;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class SkillRecastEvent extends SkillEvent {
    protected final RecastInstance recastInstance;

    protected SkillRecastEvent(CastContext castContext, RecastInstance recastInstance) {
        super(castContext.skill(), castContext);
        this.recastInstance = recastInstance;
    }

    public RecastInstance getRecastInstance() {
        return recastInstance;
    }

    public static class Start extends SkillRecastEvent implements ICancellableEvent {
        public Start(CastContext castContext, RecastInstance recastInstance) {
            super(castContext, recastInstance);
        }

        public void setRemainingCasts(int remainingCasts) {
            recastInstance.setRemainingCasts(remainingCasts);
        }

        public void setWindowDuration(int durationTicks) {
            recastInstance.setWindowDuration(durationTicks);
        }
    }

    public static class Finish extends SkillRecastEvent {
        protected final RecastResult recastResult;

        public Finish(CastContext castContext, RecastInstance recastInstance, RecastResult recastResult) {
            super(castContext, recastInstance);
            this.recastResult = recastResult;
        }

        public RecastResult getRecastResult() {
            return recastResult;
        }
    }
}
