package com.example.testmod.network;

import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.CastType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundUpdateCastingState {

    private final int spellId;
    private final int castTime;
    private final CastType castType;
    private final boolean castFinished;

    public ClientboundUpdateCastingState(int spellId, int castTime, CastType castType, boolean castFinished) {
        this.spellId = spellId;
        this.castTime = castTime;
        this.castType = castType;
        this.castFinished = castFinished;
    }

    public ClientboundUpdateCastingState(FriendlyByteBuf buf) {
        this.spellId = buf.readInt();
        this.castTime = buf.readInt();
        this.castType = CastType.values()[buf.readInt()];
        this.castFinished = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellId);
        buf.writeInt(this.castTime);
        buf.writeInt(this.castType.getValue());
        buf.writeBoolean(this.castFinished);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            //TestMod.LOGGER.debug("PacketCastingState: spellId: " + spellId + ", castTime: " + castTime + ", castFinished:" + castFinished);

            if (this.castFinished) {
                ClientMagicData.castDurationRemaining = 0;
                ClientMagicData.castDuration = 0;
                ClientMagicData.isCasting = false;
                ClientMagicData.castType = CastType.NONE;
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.stopUsingItem();
                }
            } else {
                ClientMagicData.castDurationRemaining = castTime;
                ClientMagicData.castDuration = castTime;
                ClientMagicData.isCasting = true;
                ClientMagicData.castType = castType;
            }
        });
        return true;
    }
}
