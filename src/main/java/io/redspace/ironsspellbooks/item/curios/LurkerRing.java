package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

public class LurkerRing extends SimpleDescriptiveCurio {
    public static final int COOLDOWN_IN_TICKS = 15 * 20;
    public static final float MULTIPLIER = 1.5f;

    public LurkerRing() {
        super(new Properties().tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).stacksTo(1), "ring");
    }

    @Override
    public Component getDescription(ItemStack stack) {
        double playerCooldownModifier = Minecraft.getInstance().player == null ? 1 : Minecraft.getInstance().player.getAttributeValue(COOLDOWN_REDUCTION.get());
        return Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc",
                (int) ((MULTIPLIER - 1) * 100),
                Utils.timeFromTicks((float) (COOLDOWN_IN_TICKS * (2 - Utils.softCapFormula(playerCooldownModifier))), 1)
        )).withStyle(descriptionStyle);
    }
}
