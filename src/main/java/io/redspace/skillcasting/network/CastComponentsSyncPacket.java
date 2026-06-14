package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public record CastComponentsSyncPacket(
        CasterId casterId,
        ResourceLocation skillId,
        Map<ComponentType<?>, Object> components)
        implements CustomPacketPayload {

    public static final Type<CastComponentsSyncPacket> TYPE =
            new Type<>(Skillcasting.id("sync_cast_components"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastComponentsSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(CastComponentsSyncPacket::write, CastComponentsSyncPacket::fromBuf);

    public static CastComponentsSyncPacket of(CasterRef caster, ResourceLocation skillId, Map<ComponentType<?>, Object> components) {
        return new CastComponentsSyncPacket(caster.id(), skillId, components);
    }

    private static CastComponentsSyncPacket fromBuf(RegistryFriendlyByteBuf buf) {
        CasterId id = CasterId.STREAM_CODEC.decode(buf);
        ResourceLocation skillId = buf.readResourceLocation();
        return new CastComponentsSyncPacket(id, skillId, ComponentSyncCodecs.CAST_COMPONENTS.decode(buf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
        buf.writeResourceLocation(skillId);
        ComponentSyncCodecs.CAST_COMPONENTS.encode(buf, components);
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
            applyComponents(caster.skillcastingData(), packet.skillId(), packet.components());
        });
    }

    private static void applyComponents(SkillcastingData data, ResourceLocation skillId, Map<ComponentType<?>, Object> components) {
        ActiveCast active = data.getActiveCast();
        if (active != null && skillId.equals(active.context().skill().value().getSkillId())) {
            active.context().applySynced(components);
        }
        RecastInstance recast = data.recasts().get(skillId);
        if (recast != null && recast.castContextOrNull() != null) {
            recast.castContext().applySynced(components);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
