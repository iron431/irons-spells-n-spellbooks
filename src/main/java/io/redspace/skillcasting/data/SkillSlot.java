package io.redspace.skillcasting.data;

import io.redspace.skillcasting.api.skill.AbstractSkill;
import org.jetbrains.annotations.Nullable;

/**
 * A skill stored at a fixed index inside a {@link SkillContainer}.
 */
public record SkillSlot(SkillData skillData, int index) {
    public static SkillSlot of(SkillData skillData, int index) {
        return new SkillSlot(skillData, index);
    }

    @Nullable
    public AbstractSkill getSkill() {
        return skillData.getSkill();
    }

    public int getLevel() {
        return skillData.getLevel();
    }

    public boolean isLocked() {
        return skillData.isLocked();
    }
}
