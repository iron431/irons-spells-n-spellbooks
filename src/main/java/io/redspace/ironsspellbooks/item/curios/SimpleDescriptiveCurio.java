package io.redspace.ironsspellbooks.item.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDescriptiveCurio extends CurioBaseItem {
    final @Nullable String slotIdentifier;

    public SimpleDescriptiveCurio(Properties properties, String slotIdentifier) {
        super(properties);
        this.slotIdentifier = slotIdentifier;
    }

    public SimpleDescriptiveCurio(Properties properties) {
        this(properties, null);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (slotIdentifier != null) {
            var title = Component.translatable("curios.modifiers." + this.slotIdentifier).withStyle(ChatFormatting.GOLD);
            var description = Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc")).withStyle(ChatFormatting.YELLOW);
            tooltips.add(Component.empty());
            tooltips.add(title);
            tooltips.add(description);
        }

        return super.getSlotsTooltip(tooltips, stack);
    }
}
