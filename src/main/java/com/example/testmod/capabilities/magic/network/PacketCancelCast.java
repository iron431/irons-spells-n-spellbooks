package com.example.testmod.capabilities.magic.network;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
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
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                if (playerMagicData.isCasting()) {
                    int spellId = playerMagicData.getCastingSpellId();
                    playerMagicData.resetCastingState();
                    if (triggerCooldown)
                        MagicManager.get(serverPlayer.level).addCooldown(serverPlayer,SpellType.values()[spellId]);
                    Messages.sendToPlayer(new PacketCastingState(spellId, 0, CastType.NONE, true), serverPlayer);
                }
            }
        });
        return true;
    }
}
