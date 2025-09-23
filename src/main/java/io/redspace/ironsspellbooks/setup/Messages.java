package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Deprecated wrapper-class for 1.20.1 API compat. Use {@link PacketDistributor} instead
 */
@Deprecated(forRemoval = true)
public class Messages {
    public static <MSG> void sendToServer(MSG message) {
        IronsSpellbooks.LOGGER.warn("Warning! {} packet attemping to be sent via deprecated messages channel.", message.getClass().getCanonicalName());
        PacketDistributor.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        IronsSpellbooks.LOGGER.warn("Warning! {} packet attemping to be sent via deprecated messages channel.", message.getClass().getCanonicalName());
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        IronsSpellbooks.LOGGER.warn("Warning! {} packet attemping to be sent via deprecated messages channel.", message.getClass().getCanonicalName());
        PacketDistributor.sendToAllPlayers(message);
    }

    public static <MSG> void sendToPlayersTrackingEntity(MSG message, Entity entity) {
        IronsSpellbooks.LOGGER.warn("Warning! {} packet attemping to be sent via deprecated messages channel.", message.getClass().getCanonicalName());
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }

    public static <MSG> void sendToPlayersTrackingEntity(MSG message, Entity entity, boolean sendToSource) {
        IronsSpellbooks.LOGGER.warn("Warning! {} packet attemping to be sent via deprecated messages channel.", message.getClass().getCanonicalName());
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
        if (sendToSource && entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }
}
