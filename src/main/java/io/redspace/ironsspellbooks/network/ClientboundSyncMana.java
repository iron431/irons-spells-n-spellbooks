package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncMana {

    private int playerMana = 0;
    private MagicData playerMagicData = null;


    public ClientboundSyncMana(MagicData playerMagicData) {
        //Server side only
        this.playerMagicData = playerMagicData;
    }

    public ClientboundSyncMana(FriendlyByteBuf buf) {
        playerMana = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt((int) playerMagicData.getMana());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.setMana(playerMana);
        });
        return true;
    }
}
