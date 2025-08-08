package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CancelCastPacket implements CustomPacketPayload {
    private final boolean triggerCooldown;

    public CancelCastPacket(boolean triggerCooldown) {
        this.triggerCooldown = triggerCooldown;
    }

    public CancelCastPacket(FriendlyByteBuf buf) {
        triggerCooldown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(triggerCooldown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            cancelCast(serverPlayer, triggerCooldown);
        });
        return true;
    }

    public static void cancelCast(ServerPlayer serverPlayer, boolean triggerCooldown) {
        if (serverPlayer != null) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                var spellData = playerMagicData.getCastingSpell();

                if (triggerCooldown) {
                    MagicHelper.MAGIC_MANAGER.addCooldown(serverPlayer, spellData.getSpell(), playerMagicData.getCastSource());
                }
                if (playerMagicData.getCastSource() == CastSource.SCROLL && spellData.getSpell().getCastType() == CastType.CONTINUOUS) {
                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                }
                playerMagicData.getCastingSpell().getSpell().onServerCastComplete(serverPlayer.level, spellData.getLevel(), serverPlayer, playerMagicData, true);
            }
        }
    }
}
