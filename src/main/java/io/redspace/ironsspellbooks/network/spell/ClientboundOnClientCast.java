package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOnClientCast {
    int spellId;
    int level;
    CastSource castSource;
    ICastData castData;

    public ClientboundOnClientCast(int spellId, int level, CastSource castSource, ICastData castData) {
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
 //Ironsspellbooks.logger.debug("ClientboundOnClientCast: spellId:{} level:{}", spellId, level);
            var tmp = AbstractSpell.getSpell(spellId, level).getEmptyCastData();
            tmp.readFromStream(buf);
            castData = tmp;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
        buf.writeInt(level);
        buf.writeEnum(castSource);
        if (castData instanceof ICastDataSerializable castDataSerializable) {
 //Ironsspellbooks.logger.debug("ClientboundOnClientCast.toBytes.1");
            buf.writeBoolean(true);
            castDataSerializable.writeToStream(buf);
        } else {
 //Ironsspellbooks.logger.debug("ClientboundOnClientCast.toBytes.2");
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