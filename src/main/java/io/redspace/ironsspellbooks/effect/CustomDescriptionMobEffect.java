package io.redspace.ironsspellbooks.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomDescriptionMobEffect extends MagicMobEffect {
    protected CustomDescriptionMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public static void handleCustomPotionTooltip(ItemStack itemStack, List<Component> tooltipLines, boolean isAdvanced, MobEffectInstance mobEffectInstance, CustomDescriptionMobEffect customDescriptionMobEffect) {
        var description = customDescriptionMobEffect.getDescriptionLine(mobEffectInstance);

        var header = net.minecraft.network.chat.Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE);
        var newLines = new ArrayList<Component>();
        int i = tooltipLines.indexOf(header);

        if (i < 0) {
            newLines.add(net.minecraft.network.chat.Component.empty());
            newLines.add(header);
            newLines.add(description);
            i = isAdvanced ? tooltipLines.size() - (itemStack.hasTag() ? 2 : 1) : tooltipLines.size();
        } else {
            newLines.add(description);
            i++;
        }
        tooltipLines.addAll(i, newLines);
    }

    public abstract Component getDescriptionLine(MobEffectInstance instance);
}
