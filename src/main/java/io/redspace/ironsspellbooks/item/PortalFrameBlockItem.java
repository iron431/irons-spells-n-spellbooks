package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class PortalFrameBlockItem extends BlockItem {
    private static final Component DESCRIPTION = Component.translatable("block.irons_spellbooks.portal_frame.desc").withStyle(ChatFormatting.GRAY);

    public PortalFrameBlockItem() {
        super(BlockRegistry.PORTAL_FRAME.get(), new Item.Properties().fireResistant().rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        pTooltipComponents.add(DESCRIPTION);
    }
}
