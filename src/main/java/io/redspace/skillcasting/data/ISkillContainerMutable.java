package io.redspace.skillcasting.data;

import io.redspace.skillcasting.api.skill.AbstractSkill;

public interface ISkillContainerMutable extends ISkillContainer {
    void setMaxSpellCount(int maxSpells);

    boolean setSpellAtIndex(SkillData skillData, int index);

    boolean addSpell(SkillData skillData);

    boolean removeSpellAtIndex(int index);

    boolean removeSpell(AbstractSkill spell);

    ISkillContainer toImmutable();
}
