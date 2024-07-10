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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleElixir extends DrinkableItem {
    private final Supplier<MobEffectInstance> potionEffect;

    boolean foilOverride;

    public SimpleElixir(Properties pProperties, Supplier<MobEffectInstance> potionEffect) {
        super(pProperties, SimpleElixir::applyEffect, Items.GLASS_BOTTLE, true);
        this.potionEffect = potionEffect;
    }

    public SimpleElixir(Properties pProperties, Supplier<MobEffectInstance> potionEffect, boolean foil) {
        this(pProperties, potionEffect);
        this.foilOverride = foil;
    }

    public MobEffectInstance getMobEffect() {
        return this.potionEffect.get();
    }

    private static void applyEffect(ItemStack itemStack, LivingEntity livingEntity) {
        if (itemStack.getItem() instanceof SimpleElixir elixir && elixir.potionEffect.get() != null) {
            livingEntity.addEffect(elixir.potionEffect.get());
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return super.isFoil(pStack) || foilOverride;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        Iterable<MobEffectInstance> iterable = List.of(this.getMobEffect());
        PotionContents.addPotionTooltip(iterable, pTooltipComponents::add, 1f, context.tickRate());
        if (this.potionEffect.get().getEffect() instanceof CustomDescriptionMobEffect customDescriptionMobEffect) {
            CustomDescriptionMobEffect.handleCustomPotionTooltip(pStack, pTooltipComponents, false, this.potionEffect.get(), customDescriptionMobEffect);
        }
    }
//
//    @Override
//    public int getMaxStackSize(ItemStack stack) {
//        return Items.POTION.getMaxStackSize();
//    }

}
