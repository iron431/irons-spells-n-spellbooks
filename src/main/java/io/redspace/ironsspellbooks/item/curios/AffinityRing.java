package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AffinityRing extends SimpleDescriptiveCurio {
    public static final String nbtKey = "ISBEnhance";

    public AffinityRing(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var spell = RingData.getRingData(pStack).getSpell();
        if (spell != SpellType.NONE_SPELL) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("curios.modifiers.ring").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", spell.getDisplayName().withStyle(spell.getSchoolType().getDisplayName().getStyle())).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable(this.getDescriptionId(pStack), RingData.getRingData(pStack).getSpell().getSchoolType().getDisplayName().getString());
    }
}
