package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcentrationAmuletBackport extends CurioBaseItem {
    final static net.minecraft.network.chat.Component whenWorn = Component.translatable("curios.modifiers.necklace").withStyle(ChatFormatting.GOLD);
    final net.minecraft.network.chat.Component description;

    public ConcentrationAmuletBackport(Properties properties, net.minecraft.network.chat.Component description) {
        super(properties);
        this.description = Component.literal(" ").append(description);
    }

    public ConcentrationAmuletBackport(Properties properties) {
        this(properties, Component.empty());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<net.minecraft.network.chat.Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.empty());
        //currently only rings
        pTooltipComponents.add(whenWorn);
        pTooltipComponents.add(description);
    }


}
