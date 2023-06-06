package io.redspace.ironsspellbooks.item.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class FrostwardRing extends SimpleDescriptiveCurio {
    public FrostwardRing() {
        super(new Properties().stacksTo(1), Component.translatable("item.irons_spellbooks.frostward_ring.desc").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        slotContext.entity().setTicksFrozen(0);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }
}
