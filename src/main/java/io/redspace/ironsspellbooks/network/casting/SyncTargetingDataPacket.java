package io.redspace.ironsspellbooks.network.ported.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.ClientSpellTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncTargetingDataPacket implements CustomPacketPayload {
    private final List<UUID> targetUUIDs;
    private final String spellId;
    public static final CustomPacketPayload.Type<SyncTargetingDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_targeting_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTargetingDataPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncTargetingDataPacket::write, SyncTargetingDataPacket::new);

    public SyncTargetingDataPacket(LivingEntity entity, AbstractSpell spell) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUIDs = new ArrayList<>();
        targetUUIDs.add(entity.getUUID());
        spellId = spell.getSpellId();
    }

    public SyncTargetingDataPacket(AbstractSpell spell, List<UUID> uuids) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUIDs = new ArrayList<>();
        targetUUIDs.addAll(uuids);
        spellId = spell.getSpellId();
    }

    public SyncTargetingDataPacket(FriendlyByteBuf buf) {
        //targetUuid = buf.readUUID();
        targetUUIDs = new ArrayList<>();
        spellId = buf.readUtf();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            targetUUIDs.add(buf.readUUID());
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(targetUUIDs.size());
        targetUUIDs.forEach(buf::writeUUID);
    }

    public static void handle(SyncTargetingDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientMagicData.setTargetingData(new ClientSpellTargetingData(packet.spellId, packet.targetUUIDs));
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}