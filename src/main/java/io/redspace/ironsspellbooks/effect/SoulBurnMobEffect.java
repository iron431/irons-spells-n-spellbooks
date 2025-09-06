package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

@EventBusSubscriber
public class SoulBurnMobEffect extends MagicMobEffect {
    public static final float PERCENT_PER_AMPLIFIER = 0.05f;
    public SoulBurnMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void soulBurnReduceHealing(LivingHealEvent event) {
        MobEffectInstance inst = event.getEntity().getEffect(MobEffectRegistry.SOUL_BURN);
        if (inst == null) {
            return;
        }
        event.setAmount(event.getAmount() * multiplierForAmplifier(inst.getAmplifier()));
    }

    public static float multiplierForAmplifier(int amplifier) {
        return Math.clamp(1 - (amplifier + 1) * PERCENT_PER_AMPLIFIER, 0, 1);
    }
}
