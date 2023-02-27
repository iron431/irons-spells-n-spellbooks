package com.example.testmod.network.spell;

import com.example.testmod.player.ClientSpellCastHelper;
import com.example.testmod.spells.CastSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOnClientCast {
    int spellId;
    int level;
    CastSource castSource;

    public ClientboundOnClientCast(int spellId, int level, CastSource castSource) {
        this.spellId = spellId;
        this.level = level;
        this.castSource = castSource;
    }

    public ClientboundOnClientCast(FriendlyByteBuf buf) {
        spellId = buf.readInt();
        level = buf.readInt();
        castSource = buf.readEnum(CastSource.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
        buf.writeInt(level);
        buf.writeEnum(castSource);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSpellCastHelper.handleClientboundOnClientCast(spellId, level, castSource));
        });
        return true;
    }
}