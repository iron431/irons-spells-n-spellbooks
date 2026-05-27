package io.redspace.ironsspellbooks.item.consumables;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class OminousBottleItem extends DrinkableItem {
    public OminousBottleItem(Properties pProperties) {
        super(pProperties, OminousBottleItem::applyEffect, null, false);
    }

    private static void applyEffect(ItemStack itemStack, LivingEntity livingEntity) {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 20 * 60 * 30, 0), livingEntity);
        livingEntity.level.playSound(null, livingEntity.blockPosition(), SoundRegistry.APPLY_EFFECT_BAD_OMEN.get(), livingEntity.getSoundSource());
        livingEntity.level.playSound(null, livingEntity.blockPosition(), SoundEvents.GLASS_BREAK, livingEntity.getSoundSource());
    }
}
