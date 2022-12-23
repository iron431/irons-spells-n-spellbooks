package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.data.PlayerManaProvider;
import com.example.testmod.item.WimpySpellBook;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SpellBookDataEvents {
    public static final String RESOURCE_NAME = "sbd";

    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getCapabilities().containsKey(new ResourceLocation(TestMod.MODID, RESOURCE_NAME))) {
            return;
        }

        ItemStack stack = event.getObject();
        if (stack != null && stack.getItem() instanceof WimpySpellBook) {
            event.addCapability(new ResourceLocation(TestMod.MODID, RESOURCE_NAME), new SpellBookDataProvider(SpellBookTypes.WimpySpellBook));

        }
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SpellBookData.class);
    }

}