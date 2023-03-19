package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

public class TetraActualImpl implements ITetraProxy {

    @Override
    public boolean canImbue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularBladedItem) {
            return true;
        }

        /*
                if (itemStack.getItem().builtInRegistryHolder().key().location().equals(tetraModularSword)) {
            return true;
        }

         */

        return false;
    }
}
