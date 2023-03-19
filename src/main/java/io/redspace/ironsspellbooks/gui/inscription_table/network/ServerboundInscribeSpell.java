package io.redspace.ironsspellbooks.gui.inscription_table.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundInscribeSpell {


    private final BlockPos pos;
    private final int selectedIndex;

    public ServerboundInscribeSpell(BlockPos pos, int selectedIndex) {
        //convert objects into bytes then re-read them into objects

        this.pos = pos;
        this.selectedIndex = selectedIndex;
    }

    public ServerboundInscribeSpell(FriendlyByteBuf buf) {
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
            // All validity checks should have already been done before this message could be sent
            // Keep in mind screen does not exist on server
//            InscriptionTableTile inscriptionTable = (InscriptionTableTile) ctx.getSender().level.getBlockEntity(pos);
//            if (inscriptionTable != null) {
//                inscriptionTable.doInscription(selectedIndex);
//            }

        });
        return true;
    }

}
