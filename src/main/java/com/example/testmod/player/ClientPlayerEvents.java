package com.example.testmod.player;

import com.example.testmod.TestMod;
import net.minecraftforge.event.TickEvent;

public class ClientPlayerEvents {

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.START) {
            ClientMagicData.getCooldowns().tick(1);
        }
    }
}