package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenHeldBookPacket implements CustomPacketPayload {
    private final byte hand;

    public OpenHeldBookPacket(InteractionHand hand) {
        this.hand = (byte) hand.ordinal();
    }

    public OpenHeldBookPacket(FriendlyByteBuf pBuffer) {
        this.hand = pBuffer.readByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void toBytes(FriendlyByteBuf pBuffer) {
        pBuffer.writeByte(this.hand);
    }


    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                ItemStack itemstack = player.getItemInHand(InteractionHand.values()[this.hand]);
                Minecraft.getInstance().setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
            });
        });

        return true;
    }

}