package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

public class LurkerRing extends SimpleDescriptiveCurio {
    public static final int COOLDOWN_IN_TICKS = 15 * 20;
    public static final float MULTIPLIER = 1.5f;

    public LurkerRing() {
        super(new Properties().stacksTo(1), Curios.RING_SLOT);
    }


    @Override
    public List<Component> getDescriptionLines(ItemStack stack) {
        double playerCooldownModifier = Minecraft.getInstance().player == null ? 1 : Minecraft.getInstance().player.getAttributeValue(COOLDOWN_REDUCTION.get());

        return List.of(
                Component.translatable("tooltip.irons_spellbooks.passive_ability", Utils.timeFromTicks((float) (COOLDOWN_IN_TICKS * (2 - Utils.softCapFormula(playerCooldownModifier))), 1)).withStyle(ChatFormatting.GREEN),
                getDescription(stack)
        );
    }

    @Override
    public Component getDescription(ItemStack stack) {
        return Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc",
                (int) ((MULTIPLIER - 1) * 100)
        )).withStyle(descriptionStyle);
    }
}
