package com.example.testmod;

import com.example.testmod.capabilities.ManaProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstancedEventHandler {
    @SubscribeEvent
    public void registerCaps(AttachCapabilitiesEvent<Entity> e){
        if(e.getObject() instanceof Player){
            e.addCapability(new ResourceLocation(TestMod.MODID),new ManaProvider());
            TestMod.LOGGER.info("CAP REGISTERED");
        }
    }
}
