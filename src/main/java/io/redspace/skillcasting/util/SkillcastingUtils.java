package io.redspace.skillcasting.util;

import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class SkillcastingUtils {
    public static boolean isSameItemSameComponentsIgnoreDurability(ItemStack a, ItemStack b) {
        ItemStack left = a.copy();
        ItemStack right = b.copy();
        left.remove(DataComponents.DAMAGE);
        right.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(left, right);
    }

    public static boolean shouldCancelCastOnEquipmentChange(
            ActiveCast activeCast,
            ItemStack from,
            ItemStack to,
            EquipmentSlot changedSlot) {
        if (activeCast == null) {
            return false;
        }
        String castSource = activeCast.context().get(SkillcastingComponentTypes.CAST_SOURCE.get());
        if (castSource != null
                && castSource.equals(changedSlot.getName())
                && !SkillcastingUtils.isSameItemSameComponentsIgnoreDurability(from, to)) {
            return true;
        }
        if (ISkillContainer.isSkillContainer(from)) {
            AbstractSkill skill = activeCast.context().skill().value();
            if (ISkillContainer.get(from).getIndexForSpell(skill) >= 0
                    && !SkillcastingUtils.isSameItemSameComponentsIgnoreDurability(from, to)) {
                return true;
            }
        }
        return false;
    }
}
