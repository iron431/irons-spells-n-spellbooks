package io.redspace.ironsspellbooks.item.consumables;

import io.redspace.ironsspellbooks.effect.CustomDescriptionMobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class OakskinElixir extends DrinkableItem {
    private final Supplier<MobEffectInstance> potionEffect;

    boolean foilOverride;

    public OakskinElixir(Properties pProperties, Supplier<MobEffectInstance> potionEffect) {
        super(pProperties, OakskinElixir::applyEffect, Items.GLASS_BOTTLE, true);
        this.potionEffect = potionEffect;
    }

    public OakskinElixir(Properties pProperties, Supplier<MobEffectInstance> potionEffect, boolean foil) {
        this(pProperties, potionEffect);
        this.foilOverride = foil;
    }

    public MobEffectInstance getMobEffect() {
        return this.potionEffect.get();
    }

    private static void applyEffect(ItemStack itemStack, LivingEntity livingEntity) {
        if (itemStack.getItem() instanceof OakskinElixir elixir && elixir.potionEffect.get() != null) {
            livingEntity.addEffect(elixir.potionEffect.get());
            // fixme: make version agnostic system
//            livingEntity.setData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR, Unit.INSTANCE);
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return super.isFoil(pStack) || foilOverride;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        SimpleElixir.addPotionTooltip(this.potionEffect.get(), pTooltipComponents, 1f);
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
