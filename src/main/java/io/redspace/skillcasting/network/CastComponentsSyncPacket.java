package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CastComponentsSyncPacket(
        CasterId casterId,
        Holder<AbstractSkill> skill,
        CastComponentMap components)
        implements CustomPacketPayload {

    public static final Type<CastComponentsSyncPacket> TYPE =
            new Type<>(Skillcasting.id("sync_cast_components"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastComponentsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, CastComponentsSyncPacket::casterId,
            SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, CastComponentsSyncPacket::skill,
            CastComponentMap.STREAM_CODEC, CastComponentsSyncPacket::components,
            CastComponentsSyncPacket::new);

    public static CastComponentsSyncPacket of(CasterRef caster, Holder<AbstractSkill> skill, CastComponentMap components) {
        return new CastComponentsSyncPacket(caster.id(), skill, components);
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    public static void handle(CastComponentsSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (packet.isEmpty()) {
                return;
            }
            CasterRef caster = packet.casterId().resolve(ctx.player().level());
            if (caster == null) {
                return;
            }
            applyComponents(caster.skillcastingData(), packet.skill(), packet.components());
        });
    }

    private static void applyComponents(SkillcastingData data, Holder<AbstractSkill> skill, CastComponentMap components) {
        ActiveCast active = data.getActiveCast();
        if (active != null && skill.equals(active.context().skill())) {
            active.context().components().applyFrom(components);
        }
        RecastInstance recast = data.recasts().get(skill);
        if (recast != null) {
            recast.components().applyFrom(components);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
