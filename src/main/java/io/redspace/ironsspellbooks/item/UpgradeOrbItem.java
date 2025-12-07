package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
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

    @Deprecated(forRemoval = true)
    /** THIS CONSTRUCTOR DOES NOTHING, and is here for 1.20.1 api compat. Use new upgrade orb system (See {@link UpgradeOrbTypeRegistry} for resource storage/datagen reference)*/
    public UpgradeOrbItem(UpgradeType type, Item.Properties properties) {
        this(properties, UpgradeOrbTypeRegistry.MANA);
        IronsSpellbooks.LOGGER.warn("Upgrade orb {} using legacy upgrade orb format! This is no longer valid, defaulting to mana upgrade", type.getId().toString());
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
