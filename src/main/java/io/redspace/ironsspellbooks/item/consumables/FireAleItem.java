package io.redspace.ironsspellbooks.item.consumables;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.effect.CustomDescriptionMobEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FireAleItem extends DrinkableItem {

    boolean foilOverride;

    public FireAleItem(Properties pProperties) {
        super(pProperties,
                (itemstack, livingentity) -> {
                    livingentity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 3, false, true, true));
                    livingentity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 45, 0, false, true, true));
                    livingentity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 45, 2, false, true, true));
                }, Items.GLASS_BOTTLE, false);
    }


    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Items.POTION.getMaxStackSize();
    }

}
