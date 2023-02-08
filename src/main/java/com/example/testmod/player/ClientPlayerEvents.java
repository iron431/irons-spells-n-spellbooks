package com.example.testmod.player;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {
    //
    //  Handle (Client Side) cast duration
    //
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player == Minecraft.getInstance().player) {
            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.castDurationRemaining > 0) {
                ClientMagicData.castDurationRemaining--;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        var player = event.getEntity();
        var renderer = event.getRenderer();
        var hasEvasion = ClientMagicData.getPlayerSyncedData(player.getId()).getHasEvasion();
    }
}