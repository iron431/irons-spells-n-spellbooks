package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;

/**
 * Common constants for {@link io.redspace.skillcasting.data.cast.CastSource#type()} values.
 * Each is the registry id of the data component that holds the item's skill container, which is also what the
 * skillcasting library reports for containers it gathers from equipment slots.
 */
public class SpellCastSources {
    public static final String SPELLBOOK = ComponentRegistry.SPELLBOOK_CONTAINER.getId().toString();
    public static final String SCROLL = ComponentRegistry.SCROLL_CONTAINER.getId().toString();
    public static final String IMBUE = ComponentRegistry.IMBUED_SPELL_CONTAINER.getId().toString();
}
