package com.example.testmod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class EventHandler {
    @SubscribeEvent
    public void pickupItem(EntityItemPickupEvent event) {
        System.out.println("Item picked up!");
    }
    @SubscribeEvent
    public static void RightClickItem(PlayerInteractEvent.RightClickItem event) {
        System.out.println("click");
    }
}
