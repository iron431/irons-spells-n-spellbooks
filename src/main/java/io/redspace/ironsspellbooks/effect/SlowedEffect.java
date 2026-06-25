package io.redspace.ironsspellbooks.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class SlowedEffect extends CustomDescriptionMobEffect {
    public static final float PERCENT_PER_AMPLIFIER = .025f;

    public SlowedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public Component getDescriptionLine(MobEffectInstance instance) {
        float reductionAmount = getPercentForAmplifier(instance.getAmplifier(), null);
        return Component.translatable("tooltip.irons_spellbooks.slowed_description", (int) (reductionAmount * 100)).withStyle(ChatFormatting.RED);
    }

    public static float getPercentForAmplifier(int amplifier, @Nullable LivingEntity livingEntity) {
        return (1 + amplifier) * PERCENT_PER_AMPLIFIER;
    }
}
