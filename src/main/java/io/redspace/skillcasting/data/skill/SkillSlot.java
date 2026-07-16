package io.redspace.skillcasting.data.skill;

import io.redspace.skillcasting.data.AbstractSkill;

/**
 * A skill stored at a fixed index inside a {@link SkillContainer}.
 */
public record SkillSlot(SkillData skillData, int index) {
    public static SkillSlot of(SkillData skillData, int index) {
        return new SkillSlot(skillData, index);
    }

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
