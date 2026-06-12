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

    //todo: rename to skill count
    int getMaxSpellCount();

    //todo: rename to skill count
    int getActiveSpellCount();

    int getNextAvailableIndex();

    /**
     * @return Whether this container must be equipped as an Armor/Wearable in order to grant skills, or is Handheld
     */
    boolean mustEquip();

    /**
     * @return Whether this container contributes to the Spell Wheel, or is a self-contained casting item
     */
    //todo: rename to skill wheel
    boolean isSpellWheel();

    @NotNull SkillSlot[] getAllSpells();

    @NotNull List<SkillSlot> getActiveSpells();

    @NotNull SkillData getSpellAtIndex(int index);

    int getIndexForSpell(AbstractSkill spell);

    boolean isEmpty();

    ISkillContainerMutable mutableCopy();
}
