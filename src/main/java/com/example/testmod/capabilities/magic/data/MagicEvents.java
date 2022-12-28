package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;

public class MagicEvents {

    public static final String PLAYER_MAGIC = "playerMagic";

    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerMagicProvider.PLAYER_MAGIC).isPresent()) {
                event.addCapability(new ResourceLocation(TestMod.MODID, PLAYER_MAGIC), new PlayerMagicProvider());
            }
        }
    }

//    public static void onPlayerCloned(PlayerEvent.Clone event) {
//        if (event.isWasDeath()) {
//            // We need to copyFrom the capabilities
//            event.getOriginal().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(oldStore -> {
//                event.getPlayer().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(newStore -> {
//                    newStore.copyFrom(oldStore);
//                });
//            });
//        }
//    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerMagicData.class);
    }

    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        // Don't do anything client side
        if (event.world.isClientSide) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        MagicManager.get(event.world).tick(event.world);
    }
}