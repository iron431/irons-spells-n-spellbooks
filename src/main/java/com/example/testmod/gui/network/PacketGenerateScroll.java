package com.example.testmod.gui.network;

import com.example.testmod.block.InscriptionTable.InscriptionTableTile;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGenerateScroll {


    private final BlockPos pos;
    private final int spellId;
    private final int spellLevel;

    public PacketGenerateScroll(BlockPos pos, AbstractSpell spell) {
        //convert objects into bytes then re-read them into objects

        this.pos = pos;
        if (spell == null) {
            this.spellId = -1;
            this.spellLevel = -1;
        } else {
            this.spellId = spell.getSpellType().getValue();
            this.spellLevel = spell.getLevel();

        }
    }

    public PacketGenerateScroll(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        spellId = buf.readInt();
        spellLevel = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(spellId);
        buf.writeInt(spellLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            // All validity checks should have already been done before this message could be sent
            // Keep in mind screen does not exist on server
            InscriptionTableTile inscriptionTable = (InscriptionTableTile) ctx.getSender().level.getBlockEntity(pos);
            if (inscriptionTable != null) {
                inscriptionTable.generateScroll(spellId, spellLevel);
            }

        });
        return true;
    }

}
