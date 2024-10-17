package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.item.weapons.IMultihandWeapon;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static io.redspace.ironsspellbooks.registries.ComponentRegistry.CASTING_IMPLEMENT;

public class CastingItem extends Item implements IMultihandWeapon {
    public CastingItem(Properties pProperties) {
        super(pProperties.component(CASTING_IMPLEMENT, Unit.INSTANCE));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
