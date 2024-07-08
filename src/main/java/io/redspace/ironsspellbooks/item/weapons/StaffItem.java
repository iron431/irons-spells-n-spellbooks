package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class StaffItem extends CastingItem {

    public StaffItem(Item.Properties properties) {
        super(properties);
    }

//    @Override
//    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
//        return enchantment.category == EnchantmentCategory.WEAPON;
//    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        StaffArmPose.initializeClientHelper(consumer);
    }
    public int getEnchantmentValue() {
        return 20;
    }
}
