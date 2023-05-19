package io.redspace.ironsspellbooks.item.curios;

import net.minecraft.ChatFormatting;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDescriptiveCurio extends CurioBaseItem {
    final static net.minecraft.network.chat.Component whenWornAsRing = Component.translatable("curios.modifiers.ring").withStyle(ChatFormatting.GOLD);
    final net.minecraft.network.chat.Component description;

    public SimpleDescriptiveCurio(Properties properties, net.minecraft.network.chat.Component description) {
        super(properties);
        this.description = Component.literal(" ").append(description);
    }

    public SimpleDescriptiveCurio(Properties properties) {
        this(properties, Component.empty());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<net.minecraft.network.chat.Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.empty());
        //currently only rings
        pTooltipComponents.add(whenWornAsRing);
        pTooltipComponents.add(description);
    }


}
