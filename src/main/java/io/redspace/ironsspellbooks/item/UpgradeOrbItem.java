package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeOrbItem extends Item {
    public final static Component TOOLTIP_HEADER = Component.translatable("tooltip.irons_spellbooks.upgrade_tooltip").withStyle(ChatFormatting.GRAY);
    /**
     * Upgrade Orb Types are now datadriven; UpgradeType interface is no longer supported. See {@link io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry} for datagenning custom orbs
     */
    @Deprecated(forRemoval = true)
    public UpgradeOrbItem(UpgradeType upgrade, Properties pProperties) {
        this(pProperties);
    }

    public UpgradeOrbItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack);
    }

}
