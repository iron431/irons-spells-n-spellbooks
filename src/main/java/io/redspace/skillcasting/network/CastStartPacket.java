package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientInputEvents;
import io.redspace.skillcasting.client.ClientSkillCastHelper;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CastStartPacket(
        CasterId casterId,
        ResourceLocation skillId,
        long startedAtGameTime,
        int durationTicks,
        CastComponentMap components) implements CustomPacketPayload {

    public static final Type<CastStartPacket> TYPE = new Type<>(Skillcasting.id("cast_start_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastStartPacket> STREAM_CODEC = CustomPacketPayload.codec(CastStartPacket::write, CastStartPacket::fromBuf);

    private static CastStartPacket fromBuf(RegistryFriendlyByteBuf buf) {
        CasterId id = CasterId.STREAM_CODEC.decode(buf);
        ResourceLocation skillId = buf.readResourceLocation();
        long startedAt = buf.readLong();
        int duration = buf.readVarInt();
        CastComponentMap components = CastComponentMap.STREAM_CODEC.decode(buf);
        return new CastStartPacket(id, skillId, startedAt, duration, components);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
        buf.writeResourceLocation(skillId);
        buf.writeLong(startedAtGameTime);
        buf.writeVarInt(durationTicks);
        CastComponentMap.STREAM_CODEC.encode(buf, components);
    }

    public static void handle(CastStartPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level;
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            var holder = SkillRegistry.holder(packet.skillId);
            CastContext castContext = new CastContext(holder, caster, level);
            // fixme: are we double syncing cast time?
            castContext.set(SkillcastingComponentTypes.CAST_TIME, packet.durationTicks);
            castContext.components().applyFrom(packet.components);
            data.activateCast(new ActiveCast(castContext, packet.startedAtGameTime));
            var localPlayer = context.player();
            if (localPlayer != null && packet.casterId().equals(CasterRef.entity(localPlayer).id())) {
                if (holder.value().getCastType() == CastType.CONTINUOUS) {
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