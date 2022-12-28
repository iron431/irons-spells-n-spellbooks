package com.example.testmod.gui.network;

import com.example.testmod.block.InscriptionTable.InscriptionTableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketInscribeSpell {


    private final BlockPos pos;
    private final int spellBookSlot;
    private final int scrollSlot;
    private final int selectedIndex;

    public PacketInscribeSpell(BlockPos pos, int spellBookSlot, int scrollSlot, int selectedIndex){
        //convert objects into bytes then re-read them into objects

        this.pos = pos;
        this.spellBookSlot = spellBookSlot;
        this.scrollSlot = scrollSlot;
        this.selectedIndex = selectedIndex;
    }
    public PacketInscribeSpell(FriendlyByteBuf buf){
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x,y,z);
        spellBookSlot = buf.readInt();
        scrollSlot = buf.readInt();
        selectedIndex = buf.readInt();

    }
    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(spellBookSlot);
        buf.writeInt(scrollSlot);
        buf.writeInt(selectedIndex);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            // All validity checks should have already been done before this message could be sent
            // Keep in mind screen does not exist on server
            System.out.println("packet receieved");
            InscriptionTableTile inscriptionTable = (InscriptionTableTile) ctx.getSender().level.getBlockEntity(pos);
            if(inscriptionTable!=null){
                inscriptionTable.doInscription(spellBookSlot,scrollSlot,selectedIndex);
            }

        });
        return true;
    }

}
