package io.redspace.ironsspellbooks.compat.tetra;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;


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
