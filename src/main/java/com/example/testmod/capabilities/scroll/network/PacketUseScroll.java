package com.example.testmod.capabilities.scroll.network;

import com.example.testmod.TestMod;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUseScroll {
    private final SpellType spellType;
    private final int level;

    public PacketUseScroll(SpellType spellType, int level) {
        this.spellType = spellType;
        this.level = level;
    }

    public PacketUseScroll(FriendlyByteBuf buf) {
        this.spellType = SpellType.values()[buf.readInt()];
        this.level = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellType.getValue());
        buf.writeInt(this.level);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                TestMod.LOGGER.info("PacketUseScroll.handle:" + player.getUseItem().getItem().getDescription().getString());
                TestMod.LOGGER.info("PacketUseScroll.handle:" + player.getItemInHand(player.getUsedItemHand()).getItem().getDescription().getString());

            }
        });
        return true;
    }
}