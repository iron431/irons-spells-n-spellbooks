package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
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

    public record RecastEntry(RecastConfig config, int remainingCasts, long windowEndsAtGameTime) {
    }

    public static final Type<RecastsSyncPacket> TYPE = new Type<>(Skillcasting.id("sync_recasts"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecastsSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(RecastsSyncPacket::write, RecastsSyncPacket::fromBuf);

    public static RecastsSyncPacket from(CasterRef caster, SkillcastingData data) {
        Map<ResourceLocation, RecastEntry> entries = new HashMap<>();
        for (var e : data.recasts().byId().entrySet()) {
            RecastInstance r = e.getValue();
            entries.put(e.getKey(), new RecastEntry(r.config(), r.remainingCasts(), r.windowEndsAtGameTime()));
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
            entries.put(skillId, new RecastEntry(config, remaining, windowEnd));
        }
        return new RecastsSyncPacket(id, entries);
    }

    private void write(FriendlyByteBuf buf) {
        RegistryFriendlyByteBuf rbuf = (RegistryFriendlyByteBuf) buf;
        CasterId.STREAM_CODEC.encode(rbuf, casterId);
        buf.writeVarInt(entries.size());
        for (var e : entries.entrySet()) {
            buf.writeResourceLocation(e.getKey());
            ComponentSyncCodecs.RECAST_CONFIG.encode(rbuf, e.getValue().config());
            buf.writeVarInt(e.getValue().remainingCasts());
            buf.writeLong(e.getValue().windowEndsAtGameTime());
        }
    }

    public static void handle(RecastsSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = ctx.player().level();
            CasterRef caster = packet.casterId().resolve(ctx.player().level());
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
                // fixme: uh no components?
                CastContext context = new CastContext(SkillRegistry.holder(skillId), caster, caster.level());
                recasts.put(skillId, new RecastInstance(entry.config(), entry.remainingCasts(), entry.windowEndsAtGameTime(), context));
            }
            data.applySyncedRecasts(recasts);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
