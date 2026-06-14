package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientInputEvents;
import io.redspace.skillcasting.client.ClientSkillCastHelper;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CastStartPacket(
        CasterId casterId,
        Holder<AbstractSkill> skill,
        int durationTicks,
        CastComponentMap components) implements CustomPacketPayload {

    public static final Type<CastStartPacket> TYPE = new Type<>(Skillcasting.id("cast_start_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastStartPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, CastStartPacket::casterId,
            SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, CastStartPacket::skill,
            ByteBufCodecs.VAR_INT, CastStartPacket::durationTicks,
            CastComponentMap.STREAM_CODEC, CastStartPacket::components,
            CastStartPacket::new);

    public static void handle(CastStartPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            CasterRef caster = packet.casterId().resolve(level);
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            CastContext castContext = new CastContext(packet.skill(), caster, level);
            castContext.set(SkillcastingComponentTypes.CAST_TIME, packet.durationTicks);
            castContext.components().applyFrom(packet.components);
            data.activateCast(new ActiveCast(castContext));
            var localPlayer = context.player();
            if (localPlayer != null && packet.casterId().equals(CasterRef.entity(localPlayer).id())) {
                if (packet.skill().value().getCastType() == CastType.CONTINUOUS) {
                    ClientSkillCastHelper.setSuppressRightClicks(true);
                    ClientInputEvents.hasReleasedSinceCasting = false;
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
