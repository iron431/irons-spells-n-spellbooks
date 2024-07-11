package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.tick.LevelTickEvent;


public class MagicEvents {

    public static final ResourceLocation PLAYER_MAGIC_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "player_magic");

//    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
//        if (event.getObject() instanceof ServerPlayer serverPlayer) {
//            if (!event.getObject().getCapability(PlayerMagicProvider.PLAYER_MAGIC).isPresent()) {
//                event.addCapability(PLAYER_MAGIC_RESOURCE, new PlayerMagicProvider(serverPlayer));
//            }
//        }
//    }

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

//    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
//        event.register(MagicData.class);
//    }

    public static void onWorldTick(LevelTickEvent.Post event) {
        // Don't do anything client side
        if (event.getLevel().isClientSide) {
            return;
        }

        IronsSpellbooks.MAGIC_MANAGER.tick(event.getLevel());
    }
}