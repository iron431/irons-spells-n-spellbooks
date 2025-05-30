package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal.PocketDimensionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;


public class MagicEvents {

    public static final ResourceLocation PLAYER_MAGIC_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "player_magic");

    //FIXME: look into this
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

    public static void onWorldTick(LevelTickEvent.Pre event) {
        // Don't do anything client side
        if (event.getLevel().isClientSide) {
            return;
        }

        IronsSpellbooks.MAGIC_MANAGER.tick(event.getLevel());
        PocketDimensionManager.INSTANCE.tick(event.getLevel());
    }
}