package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record RecastsSyncPacket(CasterId casterId, Map<ResourceLocation, RecastEntry> entries)
        implements CustomPacketPayload {

    public record RecastEntry(
            RecastConfig config,
            int remainingCasts,
            long windowEndsAtGameTime,
            Map<ComponentType<?>, Object> syncedComponents) {
    }

    public static final Type<RecastsSyncPacket> TYPE = new Type<>(Skillcasting.id("sync_recasts"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecastsSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(RecastsSyncPacket::write, RecastsSyncPacket::fromBuf);

    public static RecastsSyncPacket from(CasterRef caster, SkillcastingData data) {
        Map<ResourceLocation, RecastEntry> entries = new HashMap<>();
        for (var e : data.recasts().byId().entrySet()) {
            RecastInstance r = e.getValue();
            entries.put(e.getKey(), new RecastEntry(
                    r.config(),
                    r.remainingCasts(),
                    r.windowEndsAtGameTime(),
                    r.syncedComponentsForNetwork()));
        }
        return new RecastsSyncPacket(caster.id(), entries);
    }

    private static RecastsSyncPacket fromBuf(FriendlyByteBuf buf) {
        RegistryFriendlyByteBuf rbuf = (RegistryFriendlyByteBuf) buf;
        CasterId id = CasterId.STREAM_CODEC.decode(rbuf);
        int count = buf.readVarInt();
        Map<ResourceLocation, RecastEntry> entries = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation skillId = buf.readResourceLocation();
            RecastConfig config = ComponentSyncCodecs.RECAST_CONFIG.decode(rbuf);
            int remaining = buf.readVarInt();
            long windowEnd = buf.readLong();
            Map<ComponentType<?>, Object> components = ComponentSyncCodecs.CAST_COMPONENTS.decode(rbuf);
            entries.put(skillId, new RecastEntry(config, remaining, windowEnd, components));
        }
        return new RecastsSyncPacket(id, entries);
    }

    private void write(FriendlyByteBuf buf) {
        RegistryFriendlyByteBuf rbuf = (RegistryFriendlyByteBuf) buf;
        CasterId.STREAM_CODEC.encode(rbuf, casterId);
        buf.writeVarInt(entries.size());
        for (var e : entries.entrySet()) {
            buf.writeResourceLocation(e.getKey());
            RecastEntry entry = e.getValue();
            ComponentSyncCodecs.RECAST_CONFIG.encode(rbuf, entry.config());
            buf.writeVarInt(entry.remainingCasts());
            buf.writeLong(entry.windowEndsAtGameTime());
            ComponentSyncCodecs.CAST_COMPONENTS.encode(rbuf, entry.syncedComponents());
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
                RecastEntry entry = e.getValue();
                var snapshot = new io.redspace.skillcasting.api.cast.CastContext.Snapshot(
                        SkillRegistry.holder(skillId),
                        entry.syncedComponents());
                RecastInstance instance = RecastInstance.fromSnapshot(
                        entry.config(),
                        entry.remainingCasts(),
                        entry.windowEndsAtGameTime(),
                        snapshot);
                instance.rehydrate(caster);
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
