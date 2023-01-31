package com.example.testmod.network;

import com.example.testmod.player.ClientInputEvents;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSuppressRightClicks {

    public ClientboundSuppressRightClicks() {
    }

    public ClientboundSuppressRightClicks(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if(ClientInputEvents.isUseKeyDown)
                ClientMagicData.supressRightClicks = true;
        });

        return true;
    }
}
