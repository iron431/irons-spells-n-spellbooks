package io.redspace.ironsspellbooks.compat.tetra;

import net.minecraft.world.item.ItemStack;


public class TetraDummyImpl implements ITetraProxy {
    @Override
    public void initClient() {

    }

    @Override
    public boolean canImbue(ItemStack itemStack) {
        return false;
    }

    @Override
    public void handleLivingAttackEvent(LivingAttackEvent event) {

    }
}
