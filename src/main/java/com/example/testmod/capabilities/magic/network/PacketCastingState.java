package com.example.testmod.capabilities.magic.network;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCastingState {

    private final int spellId;
    private final int castTime;
    private final boolean castFinished;

    public PacketCastingState(int spellId, int castTime, boolean castFinished) {
        this.spellId = spellId;
        this.castTime = castTime;
        this.castFinished = castFinished;
    }

    public PacketCastingState(FriendlyByteBuf buf) {
        this.spellId = buf.readInt();
        this.castTime = buf.readInt();
        this.castFinished = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellId);
        buf.writeInt(this.castTime);
        buf.writeBoolean(this.castFinished);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TestMod.LOGGER.info("PacketCastingState: castTime:" + castTime + ", castFinished:" + castFinished);

            if (this.castFinished) {
                ClientMagicData.castDurationRemaining = 0;
                ClientMagicData.castDuration = 0;
                ClientMagicData.isCasting = false;
            } else {
                ClientMagicData.castDurationRemaining = castTime;
                ClientMagicData.castDuration = castTime;
                ClientMagicData.isCasting = true;
            }
        });
        return true;
    }
}
