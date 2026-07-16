package io.redspace.skillcasting.data.skill;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.data.AbstractSkill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ISkillContainer {

    static <T extends ISkillContainer> Codec<T> codec(Function4<Integer, Boolean, Boolean, List<SkillSlot>, T> constructor){
        return RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("size").forGetter(ISkillContainer::getMaxSkillCount),
                Codec.BOOL.fieldOf("wheel").forGetter(ISkillContainer::isSkillWheel),
                Codec.BOOL.fieldOf("equip").forGetter(ISkillContainer::mustEquip),
                Codec.list(SkillContainer.SPELL_SLOT_CODEC).fieldOf("data").forGetter(ISkillContainer::getActiveSkills)
        ).apply(builder, constructor));
    }

    int getMaxSkillCount();

    int getActiveSkillCount();

    int getNextAvailableIndex();

    /**
     * @return Whether this container must be equipped as an Armor/Wearable in order to grant skills, or is Handheld
     */
    boolean mustEquip();

    /**
     * @return Whether this container contributes to the Skill Wheel, or is a self-contained casting item
     */
    boolean isSkillWheel();

    /**
     * @return Array base collection of all skill slots, including empty skill slots
     */
    @NotNull SkillSlot[] getAllSkills();

    /**
     * @return Condensed form of only non-empty skill slots
     */
    @NotNull List<SkillSlot> getActiveSkills();

    @Nullable SkillData getSkillAtIndex(int index);

    int getIndexForSkill(AbstractSkill skill);

    boolean isEmpty();

    ISkillContainerMutable mutableCopy();

    static ISkillContainer create(boolean mustEquip, SkillData... skills) {
        return create(mustEquip, 0, skills);
    }

    static ISkillContainer create(boolean mustEquip, int extraSlots, SkillData... skills) {
        var container = new SkillContainer(extraSlots + skills.length, true, mustEquip).mutableCopy();
        for (SkillData data : skills) {
            container.addSpell(data);
        }
        return container.toImmutable();
    }
}
