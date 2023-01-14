package com.example.testmod.gui.network;

import com.example.testmod.block.inscription_table.InscriptionTableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRemoveSpell {


    private final BlockPos pos;
    private final int selectedIndex;

    public PacketRemoveSpell(BlockPos pos, int selectedIndex) {
        //convert objects into bytes then re-read them into objects

        this.pos = pos;
        this.selectedIndex = selectedIndex;
    }

    public PacketRemoveSpell(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        selectedIndex = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(selectedIndex);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            InscriptionTableTile inscriptionTable = (InscriptionTableTile) ctx.getSender().level.getBlockEntity(pos);
            if (inscriptionTable != null) {
                inscriptionTable.removeSelectedSpell(selectedIndex);
            }

        });
        return true;
    }

}
