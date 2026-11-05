package io.redspace.skillcasting.data.skill;

import io.redspace.skillcasting.data.AbstractSkill;

public interface ISkillContainerMutable extends ISkillContainer {
    void setMaxSkillCount(int maxSkills);

    boolean setSkillAtIndex(SkillData skillData, int index);

    boolean addSkill(SkillData skillData);

    boolean removeSkillAtIndex(int index);

    boolean removeSkill(AbstractSkill skill);

    ISkillContainer toImmutable();
}
