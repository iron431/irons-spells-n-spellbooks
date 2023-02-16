package com.example.testmod.item;

import com.example.testmod.spells.SpellRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InkItem extends Item {
    private SpellRarity rarity;

    public InkItem(SpellRarity rarity) {
        super(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS));
        this.rarity = rarity;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, lines, pIsAdvanced);
        lines.add(Component.translatable("tooltip.testmod.ink_tooltip", rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
}
