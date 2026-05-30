package io.redspace.skillcasting.api.selection;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects entries from providers during a selection rebuild.
 */
public final class SelectionAccumulator {
    private final List<SkillSelectionEntry> entries = new ArrayList<>();

    public void add(ResourceLocation skillId, String source) {
        add(skillId, 1, source);
    }

    public void add(ResourceLocation skillId, int level, String source) {
        entries.add(new SkillSelectionEntry(skillId, level, source));
    }

    public List<SkillSelectionEntry> entries() {
        return entries;
    }
}
