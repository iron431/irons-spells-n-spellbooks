package io.redspace.skillcasting.api.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-authoritative selection state: the flattened skill bar (rebuilt from equipment) plus the
 * currently selected index and its source slot.
 */
public final class SkillSelection {
    @Deprecated
    public static final String SPELLBOOK_SLOT = "spellbook";

    public static final Codec<SkillSelection> POINTER_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.optionalFieldOf("index", -1).forGetter(SkillSelection::selectedIndex),
            Codec.STRING.optionalFieldOf("source", "").forGetter(SkillSelection::selectedSource),
            Codec.INT.optionalFieldOf("lastIndex", -1).forGetter(SkillSelection::lastSelectedIndex),
            Codec.STRING.optionalFieldOf("lastSource", "").forGetter(SkillSelection::lastSelectedSource)
    ).apply(builder, (index, source, lastIndex, lastSource) -> {
        SkillSelection selection = new SkillSelection();
        selection.selectedIndex = index;
        selection.selectedSource = source;
        selection.lastSelectedIndex = lastIndex;
        selection.lastSelectedSource = lastSource;
        return selection;
    }));

    private final List<SkillSelectionEntry> entries = new ArrayList<>();
    private int selectedIndex = -1;
    private String selectedSource = "";
    private int lastSelectedIndex = -1;
    private String lastSelectedSource = "";

    public List<SkillSelectionEntry> entries() {
        return entries;
    }

    public void replaceEntries(List<SkillSelectionEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String selectedSource() {
        return selectedSource;
    }

    public int lastSelectedIndex() {
        return lastSelectedIndex;
    }

    public String lastSelectedSource() {
        return lastSelectedSource;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < entries.size()) {
            this.lastSelectedIndex = this.selectedIndex;
            this.lastSelectedSource = this.selectedSource;
            this.selectedIndex = index;
            this.selectedSource = entries.get(index).source();
        } else if (index < 0) {
            this.selectedIndex = -1;
            this.selectedSource = "";
        }
    }

    public void withSelection(int index, String source) {
        this.lastSelectedIndex = this.selectedIndex;
        this.lastSelectedSource = this.selectedSource;
        this.selectedIndex = index;
        this.selectedSource = source;
    }

    public void restorePointer(int index, String source, int lastIndex, String lastSource) {
        this.selectedIndex = index;
        this.selectedSource = source;
        this.lastSelectedIndex = lastIndex;
        this.lastSelectedSource = lastSource;
    }

    @Nullable
    public ResourceLocation selectedSkillId() {
        if (selectedIndex < 0 || selectedIndex >= entries.size()) {
            return null;
        }
        return entries.get(selectedIndex).skillId();
    }

    public int selectedSkillLevel() {
        if (selectedIndex < 0 || selectedIndex >= entries.size()) {
            return 1;
        }
        return entries.get(selectedIndex).level();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int getSkillCount() {
        return entries.size();
    }

    public List<SkillSelectionEntry> getAllSkills() {
        return entries;
    }

    @Nullable
    public AbstractSkill getSkillAt(int index) {
        if (index < 0 || index >= getSkillCount()) {
            return null;
        }
        return SkillcastingRegistries.SKILLS.get(entries.get(index).skillId());
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
