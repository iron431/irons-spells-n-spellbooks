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
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, lines, pIsAdvanced);
        lines.add(Component.translatable("tooltip.irons_spellbooks.ink_tooltip", rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
}
