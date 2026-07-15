package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;


public class MagicEvents {

     public static void onWorldTick(LevelTickEvent.Pre event) {
        // Don't do anything client side
        if (event.getLevel().isClientSide) {
            return;
        }

        IronsSpellbooks.MAGIC_MANAGER.tick(event.getLevel());
        PocketDimensionManager.INSTANCE.tick(event.getLevel());
    }
}