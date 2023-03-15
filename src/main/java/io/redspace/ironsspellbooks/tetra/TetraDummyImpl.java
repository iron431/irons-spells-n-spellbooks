package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.item.ItemStack;

public class TetraDummyImpl implements ITetraProxy {
    @Override
    public boolean canImbue(ItemStack itemStack) {
        return false;
    }
}
