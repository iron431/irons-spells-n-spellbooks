package com.example.testmod;

import com.example.testmod.item.SpellBook;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class StaticEventHandler {
    /*
    @SubscribeEvent
    public void pickupItem(EntityItemPickupEvent event) {
        System.out.println("Item picked up!");
    }
     */
    @SubscribeEvent
    public static void RightClickItem(PlayerInteractEvent.RightClickItem event) {

        if(!event.getWorld().isClientSide())
            return;
        System.out.println("click");
        Player player = event.getPlayer();
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        if(stack.getItem() instanceof SpellBook){
            System.out.println("spellbook");

        }
        //System.out.println(player);
        //System.out.println(stack);
    }
}
