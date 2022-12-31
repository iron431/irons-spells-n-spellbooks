package com.example.testmod.capabilities.magic.network;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCancelCast {

    public PacketCancelCast() {

    }

    public PacketCancelCast(FriendlyByteBuf buf) {
       buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(0);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        TestMod.LOGGER.info("PacketCancelCast");
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                if (playerMagicData.isCasting()) {
                    int spellId = playerMagicData.getCastingSpellId();
                    playerMagicData.resetCastingState();
                    Messages.sendToPlayer(new PacketCastingState(spellId, 0, true), serverPlayer);
                }
            }
        });
        return true;
    }
}
