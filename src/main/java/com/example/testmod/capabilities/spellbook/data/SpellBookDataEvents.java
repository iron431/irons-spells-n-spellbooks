package com.example.testmod.capabilities.spellbook.data;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class SpellBookDataEvents {
    public static final String RESOURCE_NAME = "sbd";

//    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
//        if (event.getCapabilities().containsKey(new ResourceLocation(TestMod.MODID, RESOURCE_NAME))) {
//            return;
//        }
//
//        ItemStack stack = event.getObject();
//        if (stack != null && stack.getItem() instanceof WimpySpellBook) {
//            event.addCapability(new ResourceLocation(TestMod.MODID, RESOURCE_NAME), new SpellBookDataProvider(SpellBookTypes.WimpySpellBook));
//
//        }
//    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SpellBookData.class);
    }

}