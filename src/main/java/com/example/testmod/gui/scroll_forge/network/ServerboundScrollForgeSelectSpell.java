package com.example.testmod.gui.scroll_forge.network;

import com.example.testmod.block.inscription_table.InscriptionTableTile;
import com.example.testmod.block.scroll_forge.ScrollForgeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundScrollForgeSelectSpell {


    private final BlockPos pos;
    private final int spellId;

    public ServerboundScrollForgeSelectSpell(BlockPos pos, int spellId) {
        this.pos = pos;
        this.spellId = spellId;
    }

    public ServerboundScrollForgeSelectSpell(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        spellId = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ScrollForgeTile scrollForgeTile = (ScrollForgeTile) ctx.getSender().level.getBlockEntity(pos);
            if (scrollForgeTile != null) {
                scrollForgeTile.setRecipeSpell(spellId);
            }

        });
        return true;
    }

}
