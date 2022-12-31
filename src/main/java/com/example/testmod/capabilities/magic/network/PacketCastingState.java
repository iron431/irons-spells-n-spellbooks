package com.example.testmod.capabilities.magic.network;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCastingState {

    private final int castTime;
    private final boolean castFinished;

    public PacketCastingState(int castTime, boolean castFinished) {
        this.castTime = castTime;
        this.castFinished = castFinished;
    }

    public PacketCastingState(FriendlyByteBuf buf) {
        this.castTime = buf.readInt();
        this.castFinished = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.castTime);
        buf.writeBoolean(this.castFinished);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TestMod.LOGGER.info("PacketCastingState: castTime:" + castTime + ", castFinished:" + castFinished);

            if (this.castTime > 0) {
                ClientMagicData.castDurationRemaining = castTime;
                ClientMagicData.castDuration = castTime;
                ClientMagicData.isCasting = !castFinished;
            }
        });
        return true;
    }
}
