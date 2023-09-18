package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.ClientSpellTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundSyncTargetingData {

    //private UUID targetUuid;
    private final UUID targetUUID;
    private final String spellId;

    public ClientboundSyncTargetingData(LivingEntity entity, AbstractSpell spell) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUID = entity.getUUID();
        spellId = spell.getSpellId();
    }

    public ClientboundSyncTargetingData(FriendlyByteBuf buf) {
        //targetUuid = buf.readUUID();
        targetUUID = buf.readUUID();
        spellId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(targetUUID);
        buf.writeUtf(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.setTargetingData(new ClientSpellTargetingData(targetUUID, spellId));
        });
        return true;
    }
}