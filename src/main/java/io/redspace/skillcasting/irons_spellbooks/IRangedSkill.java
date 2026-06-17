package io.redspace.skillcasting.irons_spellbooks;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.component.ComponentType;

import java.util.function.Supplier;

@Deprecated
// todo: i don't think this is the right approach
public interface IRangedSkill {

    // todo: somehow "register" or auto-populate cast context with applicable parameters on AbstractSkill#buildContextComponents?
    float getBaseRange(int skillLevel);

    default float getRange(CastContext castContext) {
        Supplier<ComponentType<Float>> placeholder = null;
        return castContext.find(placeholder).orElse(getBaseRange(castContext.getSkillLevel()));
    }
}
