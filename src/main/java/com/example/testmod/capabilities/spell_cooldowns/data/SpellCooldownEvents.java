//package com.example.testmod.capabilities.spell_cooldowns.data;
//
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//@Mod.EventBusSubscriber(Dist.CLIENT)
//
//public class SpellCooldownEvents {
//    @SubscribeEvent
//    public static void onWorldTick(TickEvent.WorldTickEvent event) {
//        // Don't do anything client side
//        if (event.world.isClientSide) {
//            return;
//        }
//        if (event.phase == TickEvent.Phase.START) {
//            return;
//        }
//        SpellCooldownTracker.tick();
//        //ManaManager manager = ManaManager.get(event.world);
//        //manager.tick(event.world);
//    }
//}
