package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record RecastsSyncPacket(CasterId casterId, Map<ResourceLocation, RecastInstance> entries)
        implements CustomPacketPayload {

    public static final Type<RecastsSyncPacket> TYPE = new Type<>(Skillcasting.id("sync_recasts"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecastsSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(RecastsSyncPacket::write, RecastsSyncPacket::fromBuf);

    public static RecastsSyncPacket from(CasterRef caster, SkillcastingData data) {
        return new RecastsSyncPacket(caster.id(), Map.copyOf(data.recasts().byId()));
    }

    private static RecastsSyncPacket fromBuf(RegistryFriendlyByteBuf buf) {
        CasterId id = CasterId.STREAM_CODEC.decode(buf);
        int count = buf.readVarInt();
        Map<ResourceLocation, RecastInstance> entries = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation skillId = buf.readResourceLocation();
            RecastInstance instance = RecastInstance.STREAM_CODEC.decode(buf);
            entries.put(skillId, instance);
        }
        return new RecastsSyncPacket(id, entries);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
        buf.writeVarInt(entries.size());
        for (var e : entries.entrySet()) {
            buf.writeResourceLocation(e.getKey());
            RecastInstance.STREAM_CODEC.encode(buf, e.getValue());
        }
    }

    public static void handle(RecastsSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = ctx.player().level();
            CasterRef caster = packet.casterId().resolve(level);
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            if (data == null) {
                return;
            }
            Map<ResourceLocation, RecastInstance> recasts = new HashMap<>(packet.entries().size());
            for (var e : packet.entries().entrySet()) {
                ResourceLocation skillId = e.getKey();
                RecastInstance instance = e.getValue();
                recasts.put(skillId, instance);
            }
            data.applySyncedRecasts(recasts);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
