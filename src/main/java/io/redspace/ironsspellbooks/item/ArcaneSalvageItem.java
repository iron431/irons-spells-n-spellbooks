package io.redspace.ironsspellbooks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneSalvageItem extends Item {

    public ArcaneSalvageItem() {
        super(new Properties().tab(CreativeModeTab.TAB_MATERIALS));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, lines, pIsAdvanced);
        lines.add(Component.translatable("item.irons_spellbooks.arcane_salvage_desc").withStyle(ChatFormatting.GRAY));
    }
}
