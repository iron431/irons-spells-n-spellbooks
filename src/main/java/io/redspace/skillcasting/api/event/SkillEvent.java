package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.CastContext;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.Event;

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
}
