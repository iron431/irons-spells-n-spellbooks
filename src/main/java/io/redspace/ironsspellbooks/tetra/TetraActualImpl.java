package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

import java.util.HashSet;
import java.util.Set;

public class TetraActualImpl implements ITetraProxy {

    private Set<Class> supportedItemTypes = new HashSet<>();

    @Override
    public boolean canImbue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof IModularItem) {
            return true;
        }

        return false;
    }
}
