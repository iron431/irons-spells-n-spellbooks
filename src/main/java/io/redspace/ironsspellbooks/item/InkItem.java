package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class InkItem extends Item {
    private final SpellRarity rarity;

    public InkItem(SpellRarity rarity) {
        super(ItemPropertiesHelper.material());
        this.rarity = rarity;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, lines, pIsAdvanced);
        lines.add(Component.translatable("tooltip.irons_spellbooks.ink_tooltip", rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
}
