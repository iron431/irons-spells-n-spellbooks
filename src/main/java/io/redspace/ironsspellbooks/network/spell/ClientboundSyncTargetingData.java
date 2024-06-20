package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.ClientSpellTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundSyncTargetingData {

    //private UUID targetUuid;
    private final List<UUID> targetUUIDs;
    private final String spellId;

    public ClientboundSyncTargetingData(LivingEntity entity, AbstractSpell spell) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUIDs = new ArrayList<>();
        targetUUIDs.add(entity.getUUID());
        spellId = spell.getSpellId();
    }

    public ClientboundSyncTargetingData(AbstractSpell spell, List<UUID> uuids) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUIDs = new ArrayList<>();
        targetUUIDs.addAll(uuids);
        spellId = spell.getSpellId();
    }

    public ClientboundSyncTargetingData(FriendlyByteBuf buf) {
        //targetUuid = buf.readUUID();
        targetUUIDs = new ArrayList<>();
        spellId = buf.readUtf();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            targetUUIDs.add(buf.readUUID());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        IronsSpellbooks.LOGGER.debug("ClientboundSyncTargetingData.toBytes: {} {}: {}", spellId, targetUUIDs.size(), targetUUIDs);
        buf.writeUtf(spellId);
        buf.writeInt(targetUUIDs.size());
        targetUUIDs.forEach(buf::writeUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.setTargetingData(new ClientSpellTargetingData(spellId, targetUUIDs));
        });
        return true;
    }
}