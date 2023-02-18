package com.example.testmod.network;

import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.CastSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundUpdateCastingState {

    private final int spellId;
    private final int spellLevel;
    private final int castTime;
    private final CastSource castSource;
    private final boolean castFinished;

    public ClientboundUpdateCastingState(int spellId, int spellLevel, int castTime, CastSource castSource, boolean castFinished) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.castTime = castTime;
        this.castSource = castSource;
        this.castFinished = castFinished;
    }

    public ClientboundUpdateCastingState(FriendlyByteBuf buf) {
        this.spellId = buf.readInt();
        this.spellLevel = buf.readInt();
        this.castTime = buf.readInt();
        this.castSource = buf.readEnum(CastSource.class);
        this.castFinished = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellId);
        buf.writeInt(this.spellLevel);
        buf.writeInt(this.castTime);
        buf.writeEnum(this.castSource);
        buf.writeBoolean(this.castFinished);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (this.castFinished) {
                ClientMagicData.resetClientCastState();
            } else {
                ClientMagicData.setClientCastState(spellId, spellLevel, castTime, castSource);
            }
        });
        return true;
    }
}
