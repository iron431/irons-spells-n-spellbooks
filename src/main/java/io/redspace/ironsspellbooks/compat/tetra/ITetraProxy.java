package io.redspace.ironsspellbooks.compat.tetra;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;


public interface ITetraProxy {
    void initClient();

    boolean canImbue(ItemStack itemStack);

    void handleLivingAttackEvent(LivingAttackEvent event);
}

