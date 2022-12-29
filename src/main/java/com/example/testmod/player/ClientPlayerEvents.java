package com.example.testmod.player;

import com.example.testmod.TestMod;
import net.minecraftforge.event.TickEvent;

public class ClientPlayerEvents {

    public static int ticks=0;

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient()) {
            //TestMod.LOGGER.info("onPlayerTick");
            ClientMagicData.getCooldowns().tick(1);
            ticks++;
        }
    }
}