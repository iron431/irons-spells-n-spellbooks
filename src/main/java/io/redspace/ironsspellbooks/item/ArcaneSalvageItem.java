package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.ChatFormatting;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneSalvageItem extends Item {
    private static final Component description = Component.translatable("item.irons_spellbooks.arcane_salvage_desc").withStyle(ChatFormatting.GRAY);
    public ArcaneSalvageItem() {
        super(new Properties().tab(SpellbookModCreativeTabs.SPELL_MATERIALS_TAB));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, lines, pIsAdvanced);
        lines.add(description);
    }
}
