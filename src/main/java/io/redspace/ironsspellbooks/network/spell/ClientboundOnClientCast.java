package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOnClientCast {
    String spellId;
    int level;
    CastSource castSource;
    ICastData castData;

    public ClientboundOnClientCast(String spellId, int level, CastSource castSource, ICastData castData) {
        this.spellId = spellId;
        this.level = level;
        this.castSource = castSource;
        this.castData = castData;
    }

    public ClientboundOnClientCast(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        level = buf.readInt();
        castSource = buf.readEnum(CastSource.class);
        if (buf.readBoolean()) {
            var tmp = SpellRegistry.getSpell(spellId).getEmptyCastData();
            tmp.readFromBuffer(buf);
            castData = tmp;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(level);
        buf.writeEnum(castSource);
        if (castData instanceof ICastDataSerializable castDataSerializable) {
            buf.writeBoolean(true);
            castDataSerializable.writeToBuffer(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSpellCastHelper.handleClientboundOnClientCast(spellId, level, castSource, castData));
        });
        return true;
    }
}