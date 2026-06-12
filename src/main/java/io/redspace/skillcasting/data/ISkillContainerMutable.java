package io.redspace.skillcasting.data;

import io.redspace.skillcasting.api.skill.AbstractSkill;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISkillContainerMutable extends ISkillContainer {
    void setMaxSpellCount(int maxSpells);

    boolean addSpellAtIndex(AbstractSkill spell, int level, int index, boolean locked);

    boolean addSpell(AbstractSkill spell, int level, boolean locked);

    boolean removeSpellAtIndex(int index);

    boolean removeSpell(AbstractSkill spell);

    ISkillContainer toImmutable();
}
