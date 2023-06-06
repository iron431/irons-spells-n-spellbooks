package io.redspace.ironsspellbooks.item.curios;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class PoisonwardRing extends SimpleDescriptiveCurio {
    public PoisonwardRing() {
        super(new Properties().stacksTo(1), Component.translatable("item.irons_spellbooks.poisonward_ring.desc").withStyle(ChatFormatting.YELLOW));
    }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        slotContext.entity().removeEffect(MobEffects.POISON);
    }


}
