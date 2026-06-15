package io.redspace.skillcasting.data;

import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ISkillContainer {
    static boolean isSkillContainer(@Nullable ItemStack itemStack) {
        return itemStack != null && itemStack.has(SkillcastingDataComponents.SKILL_CONTAINER);
    }

    static @Nullable ISkillContainer get(ItemStack itemStack) {
        return itemStack.get(SkillcastingDataComponents.SKILL_CONTAINER);
    }

    static void set(ItemStack itemStack, ISkillContainer container) {
        itemStack.set(SkillcastingDataComponents.SKILL_CONTAINER, container);
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

    @NotNull SkillData getSkillAtIndex(int index);

    int getIndexForSkill(AbstractSkill skill);

    boolean isEmpty();

    ISkillContainerMutable mutableCopy();
}
