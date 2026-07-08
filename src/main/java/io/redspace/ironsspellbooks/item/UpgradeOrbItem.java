package io.redspace.ironsspellbooks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class UpgradeOrbItem extends Item {
    public final static Component TOOLTIP_HEADER = Component.translatable("tooltip.irons_spellbooks.upgrade_tooltip").withStyle(ChatFormatting.GRAY);

    public UpgradeOrbItem(Properties pProperties) {
        super(pProperties);
    }
}
