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

    public SimpleElixir(Properties pProperties, Supplier<MobEffectInstance> potionEffect, boolean foil){
        this(pProperties,potionEffect);
        this.foilOverride = foil;
    }

    public MobEffectInstance getMobEffect(){
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        addPotionTooltip(this.potionEffect.get(), pTooltipComponents, 1f);
        if (this.potionEffect.get().getEffect() instanceof CustomDescriptionMobEffect customDescriptionMobEffect) {
            CustomDescriptionMobEffect.handleCustomPotionTooltip(pStack, pTooltipComponents, false, this.potionEffect.get(), customDescriptionMobEffect);
        }
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Items.POTION.getMaxStackSize();
    }

    public static void addPotionTooltip(MobEffectInstance mobeffectinstance, List<Component> pTooltips, float pDurationFactor) {
        /**
         * adapted from PotionUtils.addPotionTooltip
         */
        List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
        MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
        MobEffect mobeffect = mobeffectinstance.getEffect();
        Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
        if (!map.isEmpty()) {
            for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                AttributeModifier attributemodifier = entry.getValue();
                AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                list1.add(new Pair<>(entry.getKey(), attributemodifier1));
            }
        }

        if (mobeffectinstance.getAmplifier() > 0) {
            mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
        }

        if (mobeffectinstance.getDuration() > 20) {
            mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, pDurationFactor));
        }

        pTooltips.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));

        if (!list1.isEmpty()) {
            pTooltips.add(CommonComponents.EMPTY);
            pTooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Attribute, AttributeModifier> pair : list1) {
                AttributeModifier attributemodifier2 = pair.getSecond();
                double d0 = attributemodifier2.getAmount();
                double d1;
                if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = attributemodifier2.getAmount();
                } else {
                    d1 = attributemodifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    pTooltips.add(Component.translatable("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    pTooltips.add(Component.translatable("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }

    }

}
