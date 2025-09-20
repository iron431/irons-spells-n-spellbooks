package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class InkItem extends Item {
    private final SpellRarity rarity;
    private final Supplier<Fluid> fluid;

    public InkItem(SpellRarity rarity, Supplier<Fluid> fluid) {
        super(ItemPropertiesHelper.material());
        this.rarity = rarity;
        this.fluid = fluid;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    public static InkItem getInkForRarity(SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> (InkItem) ItemRegistry.INK_COMMON.get();
            case UNCOMMON -> (InkItem) ItemRegistry.INK_UNCOMMON.get();
            case RARE -> (InkItem) ItemRegistry.INK_RARE.get();
            case EPIC -> (InkItem) ItemRegistry.INK_EPIC.get();
            case LEGENDARY -> (InkItem) ItemRegistry.INK_LEGENDARY.get();
            default -> (InkItem) ItemRegistry.INK_COMMON.get();
        };
    }

    public Supplier<Fluid> fluid() {
        return fluid;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("tooltip.irons_spellbooks.ink_tooltip", rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
}
