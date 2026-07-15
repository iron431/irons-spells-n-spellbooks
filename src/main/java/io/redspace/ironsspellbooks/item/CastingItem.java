package io.redspace.ironsspellbooks.item;

import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CastingItem extends Item {
    public CastingItem(Properties pProperties) {
        super(pProperties.component(SkillcastingDataComponents.CASTING_IMPLEMENT, Unit.INSTANCE));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
