package io.redspace.ironsspellbooks.tetra;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.tetra.effects.FreezeTetraEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

public class TetraActualImpl implements ITetraProxy {
    private ItemEffect freezeOnHit = ItemEffect.get(IronsSpellbooks.MODID + ":freeze");

    @Override
    public boolean canImbue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularBladedItem) {
            return true;
        }
        return false;
    }

    @Override
    public void handleLivingAttackEvent(LivingAttackEvent event) {
        LivingEntity attackedEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        /*
        Attacker Effects
         */
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldStack = livingAttacker.getMainHandItem();
            if (heldStack.getItem() instanceof ModularItem item) {

                FreezeTetraEffect.handleFreezeEffect(livingAttacker, attackedEntity, heldStack);
            }
        }
    }
}
