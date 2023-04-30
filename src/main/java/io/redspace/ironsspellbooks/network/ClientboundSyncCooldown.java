package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncCooldown {
    private final int spellId;
    private final int duration;

    public ClientboundSyncCooldown(int spellId, int duration) {

        this.spellId = spellId;
        this.duration = duration;
    }

    public ClientboundSyncCooldown(FriendlyByteBuf buf) {
        spellId = buf.readInt();
        duration = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
        buf.writeInt(duration);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
 //Ironsspellbooks.logger.debug("ClientboundSyncCooldown: {}", duration);
            ClientMagicData.getCooldowns().addCooldown(SpellType.values()[spellId], duration);
        });
        return true;
    }
}
