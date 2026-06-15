package io.redspace.skillcasting.client;

import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;

public interface ClientSkillTicker {
    void tick(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast);
}
