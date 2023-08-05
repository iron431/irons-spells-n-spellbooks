package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class UpgradeOrbItem extends Item {
    private final UpgradeType upgrade;
    private final static Component TOOLTIP_HEADER = Component.translatable("tooltip.irons_spellbooks.upgrade_tooltip").withStyle(ChatFormatting.GRAY);
    private final Component TOOLTIP_TEXT;

    public UpgradeOrbItem(UpgradeType upgrade, Properties pProperties) {
        super(pProperties);
        this.upgrade = upgrade;
        TOOLTIP_TEXT = Component.literal(" ").append(Component.translatable("attribute.modifier.plus." + upgrade.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(upgrade.getAmountPerUpgrade() * (upgrade.getOperation() == AttributeModifier.Operation.ADDITION ? 1 : 100)), Component.translatable(upgrade.getAttribute().getDescriptionId())).withStyle(ChatFormatting.BLUE));
    }

    public UpgradeType getUpgradeType() {
        return this.upgrade;
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(TOOLTIP_HEADER);
        pTooltipComponents.add(TOOLTIP_TEXT);

    }
}
