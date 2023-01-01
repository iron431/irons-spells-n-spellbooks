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

public class PacketSyncCooldownToClient {
    private final int spellId;
    private final int duration;

    public PacketSyncCooldownToClient(int spellId,int duration) {

        this.spellId=spellId;
        this.duration=duration;
    }

    public PacketSyncCooldownToClient(FriendlyByteBuf buf) {
        spellId = buf.readInt();
        duration = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
        buf.writeInt(duration);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        TestMod.LOGGER.info("PacketCancelCast");
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.getCooldowns().addCooldown(SpellType.values()[spellId], duration);
            TestMod.LOGGER.info("Client Sync triggering Cooldown");

        });
        return true;
    }
}
