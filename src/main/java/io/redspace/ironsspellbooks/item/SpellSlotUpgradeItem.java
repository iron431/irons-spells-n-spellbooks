package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpellSlotUpgradeItem extends Item {
    private final int maxSlots;
    private final Component description;

    @Deprecated
    public SpellSlotUpgradeItem(int maxSlotsToUpgradeTo) {
        this(maxSlotsToUpgradeTo, ItemPropertiesHelper.material().rarity(Rarity.RARE));
    }

    public SpellSlotUpgradeItem(int maxSlotsToUpgradeTo, Item.Properties properties) {
        super(properties);
        this.maxSlots = maxSlotsToUpgradeTo;
        this.description = Component.translatable("item.irons_spellbooks.spell_slot_upgrade_desc", maxSlotsToUpgradeTo).withStyle(ChatFormatting.GRAY);
    }

    public int maxSlots() {
        return maxSlots;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level context, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, lines, pIsAdvanced);
        lines.add(description);
    }
}
