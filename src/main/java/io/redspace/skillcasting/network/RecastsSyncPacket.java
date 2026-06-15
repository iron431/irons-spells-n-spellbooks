package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record RecastsSyncPacket(CasterId casterId, Map<Holder<AbstractSkill>, RecastInstance> entries)
        implements CustomPacketPayload {

    public static final Type<RecastsSyncPacket> TYPE = new Type<>(Skillcasting.id("sync_recasts"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Holder<AbstractSkill>, RecastInstance>> RECASTS_MAP =
            ByteBufCodecs.map(HashMap::new, SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, RecastInstance.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, RecastsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, RecastsSyncPacket::casterId,
            RECASTS_MAP, RecastsSyncPacket::entries,
            RecastsSyncPacket::new);

    public static RecastsSyncPacket from(CasterRef caster, SkillcastingData data) {
        return new RecastsSyncPacket(caster.id(), Map.copyOf(data.recasts().asMap()));
    }

    public static void handle(RecastsSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(ctx.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            if (data == null) {
                return;
            }
            data.applySyncedRecasts(packet.entries);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
