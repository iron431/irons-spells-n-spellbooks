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
        registrar.playToClient(SyncCooldownPacket.TYPE, SyncCooldownPacket.STREAM_CODEC, SyncCooldownPacket::handle);
        registrar.playToClient(SyncAllCooldownsPacket.TYPE, SyncAllCooldownsPacket.STREAM_CODEC, SyncAllCooldownsPacket::handle);
        registrar.playToClient(SyncRecastPacket.TYPE, SyncRecastPacket.STREAM_CODEC, SyncRecastPacket::handle);
        registrar.playToClient(SyncAllRecastsPacket.TYPE, SyncAllRecastsPacket.STREAM_CODEC, SyncAllRecastsPacket::handle);
        registrar.playToClient(SyncCastComponentsPacket.TYPE, SyncCastComponentsPacket.STREAM_CODEC, SyncCastComponentsPacket::handle);

        registrar.playToServer(ServerboundCastSelectedSkillPacket.TYPE, ServerboundCastSelectedSkillPacket.STREAM_CODEC, ServerboundCastSelectedSkillPacket::handle);
        registrar.playToServer(ServerboundCancelSkillCastPacket.TYPE, ServerboundCancelSkillCastPacket.STREAM_CODEC, ServerboundCancelSkillCastPacket::handle);
        registrar.playToServer(ServerboundQuickCastSkillPacket.TYPE, ServerboundQuickCastSkillPacket.STREAM_CODEC, ServerboundQuickCastSkillPacket::handle);
        registrar.playToServer(ServerboundSelectSkillPacket.TYPE, ServerboundSelectSkillPacket.STREAM_CODEC, ServerboundSelectSkillPacket::handle);
        registrar.playToClient(SyncSelectionPacket.TYPE, SyncSelectionPacket.STREAM_CODEC, SyncSelectionPacket::handle);
        registrar.playToClient(DebugHudTogglePacket.TYPE, DebugHudTogglePacket.STREAM_CODEC, DebugHudTogglePacket::handle);
    }
}
