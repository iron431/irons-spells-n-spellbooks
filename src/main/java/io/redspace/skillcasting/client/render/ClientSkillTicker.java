package io.redspace.skillcasting.client.render;

import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.SkillcastingData;

public interface ClientSkillTicker {
    void tick(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast);
}
