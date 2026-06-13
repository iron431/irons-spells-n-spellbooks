package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record CastStartPacket(
        CasterId casterId,
        ResourceLocation skillId,
        long startedAtGameTime,
        int durationTicks,
        Map<ComponentType<?>, Object> components) implements CustomPacketPayload {

    public static final Type<CastStartPacket> TYPE = new Type<>(Skillcasting.id("cast_start_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastStartPacket> STREAM_CODEC = CustomPacketPayload.codec(CastStartPacket::write, CastStartPacket::fromBuf);

    private static CastStartPacket fromBuf(RegistryFriendlyByteBuf buf) {
        CasterId id = CasterId.STREAM_CODEC.decode(buf);
        ResourceLocation skillId = buf.readResourceLocation();
        long startedAt = buf.readLong();
        int duration = buf.readVarInt();
        Map<ComponentType<?>, Object> components = ComponentSyncCodecs.CAST_COMPONENTS.decode(buf);
        return new CastStartPacket(id, skillId, startedAt, duration, components);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
        buf.writeResourceLocation(skillId);
        buf.writeLong(startedAtGameTime);
        buf.writeVarInt(durationTicks);
        ComponentSyncCodecs.CAST_COMPONENTS.encode(buf, components);
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
            castContext.set(SkillcastingComponentTypes.CAST_TIME.get(), packet.durationTicks);
            castContext.applySynced(packet.components);
            data.activateCast(new ActiveCast(castContext, packet.startedAtGameTime));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
