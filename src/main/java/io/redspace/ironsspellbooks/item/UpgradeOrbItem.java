package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.effect.IBackwardsCompatDefaultNbtItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeOrbItem extends Item implements IBackwardsCompatDefaultNbtItem {
    public final static Component TOOLTIP_HEADER = Component.translatable("tooltip.irons_spellbooks.upgrade_tooltip").withStyle(ChatFormatting.GRAY);

    public final ResourceKey<UpgradeOrbType> upgradeOrbTypeResourceKey;

    public UpgradeOrbItem(Properties pProperties, ResourceKey<UpgradeOrbType> upgradeOrbTypeResourceKey) {
        super(pProperties);
        this.upgradeOrbTypeResourceKey = upgradeOrbTypeResourceKey;
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack);
    }

    @Override
    public void setupItem(ItemStack stack) {
        UpgradeOrbTypeData.set(stack, new UpgradeOrbTypeData(upgradeOrbTypeResourceKey));
    }
}
