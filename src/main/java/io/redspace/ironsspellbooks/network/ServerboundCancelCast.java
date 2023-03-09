package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.setup.Messages;
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
        IronsSpellbooks.LOGGER.debug("PacketCancelCast.handle");
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
                IronsSpellbooks.LOGGER.debug("PacketCancelCast.cancelCast currently casting");
                int spellId = playerMagicData.getCastingSpellId();

                if (triggerCooldown) {
                    MagicManager.get(serverPlayer.level).addCooldown(serverPlayer, SpellType.values()[spellId], playerMagicData.getCastSource());
                }

                playerMagicData.getCastingSpell().onServerCastComplete(serverPlayer.level, serverPlayer, playerMagicData);
                playerMagicData.resetCastingState();

                Messages.sendToPlayer(new ClientboundUpdateCastingState(spellId, 0, 0, playerMagicData.getCastSource(), true), serverPlayer);
                if (SpellType.values()[spellId].getCastType() == CastType.CONTINUOUS)
                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
            }
        }
    }
}
