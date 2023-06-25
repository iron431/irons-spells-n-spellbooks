package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundCancelCast {
    private final boolean triggerCooldown;

    public ServerboundCancelCast(boolean triggerCooldown) {

        this.triggerCooldown = triggerCooldown;
    }

    public ServerboundCancelCast(FriendlyByteBuf buf) {
        triggerCooldown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(triggerCooldown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
 //Ironsspellbooks.logger.debug("PacketCancelCast.handle");
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            cancelCast(serverPlayer, triggerCooldown);
        });
        return true;
    }

    public static void cancelCast(ServerPlayer serverPlayer, boolean triggerCooldown) {
        if (serverPlayer != null) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
 //Ironsspellbooks.logger.debug("PacketCancelCast.cancelCast currently casting");
                int spellId = playerMagicData.getCastingSpellId();

                if (triggerCooldown) {
                    MagicManager.get(serverPlayer.level()).addCooldown(serverPlayer, SpellType.values()[spellId], playerMagicData.getCastSource());
                }

 //Ironsspellbooks.logger.debug("ServerBoundCancelCast.cancelCast");
                playerMagicData.getCastingSpell().onServerCastComplete(serverPlayer.level(), serverPlayer, playerMagicData, true);
                serverPlayer.stopUsingItem();
                if (SpellType.values()[spellId].getCastType() == CastType.CONTINUOUS)
                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
            }
        }
    }
}
