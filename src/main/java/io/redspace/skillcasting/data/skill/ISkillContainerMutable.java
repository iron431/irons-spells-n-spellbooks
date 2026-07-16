package io.redspace.skillcasting.data.skill;

import io.redspace.skillcasting.data.AbstractSkill;

public interface ISkillContainerMutable extends ISkillContainer {
    void setMaxSpellCount(int maxSpells);

    boolean setSpellAtIndex(SkillData skillData, int index);

    boolean addSpell(SkillData skillData);

    boolean removeSpellAtIndex(int index);

    boolean removeSpell(AbstractSkill spell);

    ISkillContainer toImmutable();
}
