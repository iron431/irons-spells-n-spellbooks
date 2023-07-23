package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class FirewardRing extends SimpleDescriptiveCurio {
    public FirewardRing() {
        super(new Item.Properties().tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).stacksTo(1), "ring");
    }


    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        slotContext.entity().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100000, 0, false, false, false));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        slotContext.entity().removeEffect(MobEffects.FIRE_RESISTANCE);
    }

    //    @Override
//    public void curioTick(SlotContext slotContext, ItemStack stack) {
//        super.curioTick(slotContext, stack);
//        slotContext.entity().clearFire();
//    }
}
