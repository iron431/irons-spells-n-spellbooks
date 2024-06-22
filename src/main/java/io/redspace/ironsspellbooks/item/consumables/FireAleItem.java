package io.redspace.ironsspellbooks.item.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FireAleItem extends DrinkableItem {

    boolean foilOverride;

    public FireAleItem(Properties pProperties) {
        super(pProperties,
                (itemstack, livingentity) -> {
                    livingentity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 3, false, true, true));
                    livingentity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 45, 0, false, true, true));
                    livingentity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 45, 2, false, true, true));
                }, Items.GLASS_BOTTLE, false);
    }


    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Items.POTION.getMaxStackSize();
    }

}
