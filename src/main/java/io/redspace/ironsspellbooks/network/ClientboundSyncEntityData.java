package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncEntityData {
    SyncedSpellData syncedSpellData;
    int entityId;

    public ClientboundSyncEntityData(SyncedSpellData syncedSpellData, AbstractSpellCastingMob entity) {
        this.syncedSpellData = syncedSpellData;
        this.entityId = entity.getId();
    }

    public ClientboundSyncEntityData(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        syncedSpellData = SyncedSpellData.SYNCED_SPELL_DATA.read(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        SyncedSpellData.SYNCED_SPELL_DATA.write(buf, syncedSpellData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ClientMagicData.handleAbstractCastingMobSyncedData(entityId, syncedSpellData);
        });

        return true;
    }
}
