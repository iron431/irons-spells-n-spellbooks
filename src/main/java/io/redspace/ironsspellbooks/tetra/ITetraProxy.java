package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public interface ITetraProxy {
    void initClient();

    boolean canImbue(ItemStack itemStack);

    void handleLivingAttackEvent(LivingAttackEvent event);
}

