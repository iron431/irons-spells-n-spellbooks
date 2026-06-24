package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nullable;


@EventBusSubscriber
public class OakskinEffect extends CustomDescriptionMobEffect {
    public static final float REDUCTION_PER_LEVEL = .05f;
    public static final float BASE_REDUCTION = .10f;
    public static final float SLOWNESS_MAGNITUDE = .25f;

    public OakskinEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public Component getDescriptionLine(MobEffectInstance instance) {
        float reductionAmount = getReductionAmount(instance.getAmplifier(), null);
        return Component.translatable("tooltip.irons_spellbooks.oakskin_description", (int) (reductionAmount * 100)).withStyle(ChatFormatting.BLUE);
    }

    @SubscribeEvent
    public static void reduceDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        var effect = entity.getEffect(MobEffectRegistry.OAKSKIN);
        if (effect != null) {
            float before = event.getAmount();
            float multiplier = 1 - getReductionAmount(effect.getAmplifier(), entity);
            event.setAmount(event.getAmount() * multiplier);
            IronsSpellbooks.LOGGER.debug("OakskinEffect.reduceDamage {}%: {}->{}", (int) (getReductionAmount(effect.getAmplifier(), entity) * 100), before, event.getAmount());
        }
    }

    public static float getReductionAmount(int amplifier, @Nullable LivingEntity livingEntity) {
        float multiplier = SpellRegistry.OAKSKIN_SPELL.get().getEntityPowerMultiplier(livingEntity);
        if (livingEntity != null && livingEntity.hasData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR)) {
            // prevent elixir from scaling with spell power
            multiplier = 1;
        }
        return Math.min(0.75f, (BASE_REDUCTION + REDUCTION_PER_LEVEL * amplifier) * multiplier);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectRemoved(pLivingEntity, pAmplifier);
        pLivingEntity.removeData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR);
    }
}
