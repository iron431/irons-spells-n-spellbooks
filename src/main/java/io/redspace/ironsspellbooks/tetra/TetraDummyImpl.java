package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class TetraDummyImpl implements ITetraProxy {
    @Override
    public boolean canImbue(ItemStack itemStack) {
        return false;
    }

    @Override
    public void handleLivingAttackEvent(LivingAttackEvent event) {

    }
}
