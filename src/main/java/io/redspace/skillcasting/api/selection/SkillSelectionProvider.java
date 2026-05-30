package io.redspace.skillcasting.api.selection;

import io.redspace.skillcasting.api.cast.CasterRef;

/**
 * Contributes selectable skills from one source. Multiple providers are aggregated in registration
 * order to form the caster's full selection.
 */
@FunctionalInterface
public interface SkillSelectionProvider {
    void collect(CasterRef caster, SelectionAccumulator acc);
}
