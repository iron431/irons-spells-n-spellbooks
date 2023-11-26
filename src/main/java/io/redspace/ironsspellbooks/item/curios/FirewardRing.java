package io.redspace.ironsspellbooks.item.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class FirewardRing extends SimpleDescriptiveCurio {
    public FirewardRing() {
        super(new Item.Properties().stacksTo(1), "ring");
    }
}
