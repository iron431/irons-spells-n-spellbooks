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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SyncRecastPacket(CasterId casterId, Holder<AbstractSkill> skill, @Nullable RecastInstance recast)
        implements CustomPacketPayload {

    public static final Type<SyncRecastPacket> TYPE = new Type<>(Skillcasting.id("sync_recast"));

    private static final StreamCodec<RegistryFriendlyByteBuf, @Nullable RecastInstance> NULLABLE_RECAST =
            ByteBufCodecs.optional(RecastInstance.STREAM_CODEC)
                    .map(optional -> optional.orElse(null), instance -> Optional.ofNullable(instance));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecastPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, SyncRecastPacket::casterId,
            SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, SyncRecastPacket::skill,
            NULLABLE_RECAST, SyncRecastPacket::recast,
            SyncRecastPacket::new);

    public static SyncRecastPacket set(CasterRef caster, Holder<AbstractSkill> skill, RecastInstance instance) {
        return new SyncRecastPacket(caster.id(), skill, instance);
    }

    public static SyncRecastPacket remove(CasterRef caster, Holder<AbstractSkill> skill) {
        return new SyncRecastPacket(caster.id(), skill, null);
    }

    public static void handle(SyncRecastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            if (data != null) {
                Holder<AbstractSkill> skill = packet.skill();
                data.applySyncedRecast(skill, packet.recast());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
