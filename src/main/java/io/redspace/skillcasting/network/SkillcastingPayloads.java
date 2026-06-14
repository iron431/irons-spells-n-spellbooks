package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SkillcastingPayloads {
    private SkillcastingPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Skillcasting.NAMESPACE).versioned("1.0.0").optional();

        registrar.playToClient(CastStartPacket.TYPE, CastStartPacket.STREAM_CODEC, CastStartPacket::handle);
        registrar.playToClient(CastStopPacket.TYPE, CastStopPacket.STREAM_CODEC, CastStopPacket::handle);
        registrar.playToClient(CooldownsSyncPacket.TYPE, CooldownsSyncPacket.STREAM_CODEC, CooldownsSyncPacket::handle);
        registrar.playToClient(RecastsSyncPacket.TYPE, RecastsSyncPacket.STREAM_CODEC, RecastsSyncPacket::handle);
        registrar.playToClient(CastComponentsSyncPacket.TYPE, CastComponentsSyncPacket.STREAM_CODEC, CastComponentsSyncPacket::handle);

        registrar.playToServer(ServerboundCastSelectedSkillPacket.TYPE, ServerboundCastSelectedSkillPacket.STREAM_CODEC, ServerboundCastSelectedSkillPacket::handle);
        registrar.playToServer(ServerboundCancelSkillCastPacket.TYPE, ServerboundCancelSkillCastPacket.STREAM_CODEC, ServerboundCancelSkillCastPacket::handle);
        registrar.playToServer(ServerboundQuickCastSkillPacket.TYPE, ServerboundQuickCastSkillPacket.STREAM_CODEC, ServerboundQuickCastSkillPacket::handle);
        registrar.playToServer(ServerboundSelectSkillPacket.TYPE, ServerboundSelectSkillPacket.STREAM_CODEC, ServerboundSelectSkillPacket::handle);
        registrar.playToClient(SelectionSyncPacket.TYPE, SelectionSyncPacket.STREAM_CODEC, SelectionSyncPacket::handle);
        registrar.playToClient(DebugHudTogglePacket.TYPE, DebugHudTogglePacket.STREAM_CODEC, DebugHudTogglePacket::handle);
    }
}
