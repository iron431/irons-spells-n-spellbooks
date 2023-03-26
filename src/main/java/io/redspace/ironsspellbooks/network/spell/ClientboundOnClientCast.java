package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.CastDataSerializable;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOnClientCast {
    int spellId;
    int level;
    CastSource castSource;
    CastData castData;

    public ClientboundOnClientCast(int spellId, int level, CastSource castSource, CastData castData) {
        this.spellId = spellId;
        this.level = level;
        this.castSource = castSource;
        this.castData = castData;
    }

    public ClientboundOnClientCast(FriendlyByteBuf buf) {
        spellId = buf.readInt();
        level = buf.readInt();
        castSource = buf.readEnum(CastSource.class);
        if (buf.readBoolean()) {
            var tmp = AbstractSpell.getSpell(spellId, level).getEmptyCastData();
            tmp.readFromStream(buf);
            castData = tmp;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
        buf.writeInt(level);
        buf.writeEnum(castSource);
        if (castData instanceof CastDataSerializable castDataSerializable) {
            buf.writeBoolean(true);
            castDataSerializable.writeToStream(buf);
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