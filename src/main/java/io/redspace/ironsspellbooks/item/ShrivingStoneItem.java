package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ShrivingStoneItem extends Item {
    private static final Component description = Component.translatable("item.irons_spellbooks.shriving_stone_desc").withStyle(ChatFormatting.GRAY);

    public ShrivingStoneItem() {
        super(ItemPropertiesHelper.material());
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, lines, pIsAdvanced);
        lines.add(description);
    }
}
