package com.example.testmod.network;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.item.Scroll;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCancelCast {
    private final boolean triggerCooldown;

    public PacketCancelCast(boolean triggerCooldown) {

        this.triggerCooldown = triggerCooldown;
    }

    public PacketCancelCast(FriendlyByteBuf buf) {
        triggerCooldown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(triggerCooldown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        TestMod.LOGGER.debug("PacketCancelCast.handle");
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
                TestMod.LOGGER.debug("PacketCancelCast.cancelCast currently casting");
                int spellId = playerMagicData.getCastingSpellId();

                if (triggerCooldown) {
                    MagicManager.get(serverPlayer.level).addCooldown(serverPlayer, SpellType.values()[spellId], playerMagicData.getCastSource());
                    AbstractSpell.getSpell(spellId, 0).onCastComplete(serverPlayer.level, serverPlayer, playerMagicData);
                }

                playerMagicData.resetCastingState();

                Messages.sendToPlayer(new PacketCastingState(spellId, 0, CastType.NONE, true), serverPlayer);
                if (SpellType.values()[spellId].getCastType() == CastType.CONTINUOUS)
                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
            }
        }
    }
}
