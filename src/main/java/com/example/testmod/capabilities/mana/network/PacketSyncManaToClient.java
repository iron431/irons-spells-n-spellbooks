package com.example.testmod.capabilities.mana.network;

import com.example.testmod.capabilities.mana.client.ClientManaData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncManaToClient {

    private final int playerMana;

    public PacketSyncManaToClient(int playerMana) {
        this.playerMana = playerMana;
    }

    public PacketSyncManaToClient(FriendlyByteBuf buf) {
        playerMana = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerMana);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are client side.
            // Be very careful not to access client-only classes here! (like Minecraft) because
            // this packet needs to be available server-side too
            ClientManaData.set(playerMana);
        });
        return true;
    }
}
