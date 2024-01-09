package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.logging.Level;

@Mod.EventBusSubscriber
public class OakskinEffect extends CustomDescriptionMobEffect {
    public static final float REDUCTION_PER_LEVEL = .05f;
    public static final float BASE_REDUCTION = .10f;

    public OakskinEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public Component getDescriptionLine(MobEffectInstance instance) {
        int amp = instance.getAmplifier() + 1;
        float reductionAmount = getReductionAmount(amp);
        return Component.translatable("tooltip.irons_spellbooks.oakskin_description", (int) (reductionAmount * 100)).withStyle(ChatFormatting.BLUE);
    }

    @SubscribeEvent
    public static void reduceDamage(LivingDamageEvent event) {
        var entity = event.getEntity();
        var effect = entity.getEffect(MobEffectRegistry.OAKSKIN.get());
        if (effect != null) {
            int lvl = effect.getAmplifier() + 1;
            float before = event.getAmount();
            float multiplier = 1 - getReductionAmount(lvl);
            event.setAmount(event.getAmount() * multiplier);
            //IronsSpellbooks.LOGGER.debug("OakskinEffect.reduceDamage: {}->{}", before, event.getAmount());
        }
    }

    public static float getReductionAmount(int level) {
        return BASE_REDUCTION + REDUCTION_PER_LEVEL * level;
    }
}
