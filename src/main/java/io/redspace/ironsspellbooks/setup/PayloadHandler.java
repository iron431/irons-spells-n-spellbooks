package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.network.ported.*;
import io.redspace.ironsspellbooks.network.ported.casting.*;
import io.redspace.ironsspellbooks.network.ported.particles.*;
import io.redspace.ironsspellbooks.network.ported.spells.GuidingBoltManagerStartTrackingPacket;
import io.redspace.ironsspellbooks.network.ported.spells.GuidingBoltManagerStopTrackingPacket;
import io.redspace.ironsspellbooks.network.ported.spells.LearnSpellPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = IronsSpellbooks.MODID)
public class PayloadHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(IronsSpellbooks.MODID).versioned("1.0.0").optional();

        //GENERAL
        payloadRegistrar.playToClient(AddMotionToPlayerPacket.TYPE, AddMotionToPlayerPacket.STREAM_CODEC, AddMotionToPlayerPacket::handle);
        payloadRegistrar.playToClient(EntityEventPacket.TYPE, EntityEventPacket.STREAM_CODEC, EntityEventPacket::handle);
        payloadRegistrar.playToClient(EquipmentChangedPacket.TYPE, EquipmentChangedPacket.STREAM_CODEC, EquipmentChangedPacket::handle);
        payloadRegistrar.playToClient(OpenEldritchScreenPacket.TYPE, OpenEldritchScreenPacket.STREAM_CODEC, OpenEldritchScreenPacket::handle);
        payloadRegistrar.playToClient(SyncAnimationPacket.TYPE, SyncAnimationPacket.STREAM_CODEC, SyncAnimationPacket::handle);
        payloadRegistrar.playToClient(SyncCameraShakePacket.TYPE, SyncCameraShakePacket.STREAM_CODEC, SyncCameraShakePacket::handle);
        payloadRegistrar.playToClient(SyncManaPacket.TYPE, SyncManaPacket.STREAM_CODEC, SyncManaPacket::handle);

        //PARTICLES
        payloadRegistrar.playToClient(AbsorptionParticlesPacket.TYPE, AbsorptionParticlesPacket.STREAM_CODEC, AbsorptionParticlesPacket::handle);
        payloadRegistrar.playToClient(BloodSiphonParticlesPacket.TYPE, BloodSiphonParticlesPacket.STREAM_CODEC, BloodSiphonParticlesPacket::handle);
        payloadRegistrar.playToClient(FieryExplosionParticlesPacket.TYPE, FieryExplosionParticlesPacket.STREAM_CODEC, FieryExplosionParticlesPacket::handle);
        payloadRegistrar.playToClient(FortifyAreaParticlesPacket.TYPE, FortifyAreaParticlesPacket.STREAM_CODEC, FortifyAreaParticlesPacket::handle);
        payloadRegistrar.playToClient(FrostStepParticlesPacket.TYPE, FrostStepParticlesPacket.STREAM_CODEC, FrostStepParticlesPacket::handle);
        payloadRegistrar.playToClient(HealParticlesPacket.TYPE, HealParticlesPacket.STREAM_CODEC, HealParticlesPacket::handle);
        payloadRegistrar.playToClient(OakskinParticlesPacket.TYPE, OakskinParticlesPacket.STREAM_CODEC, OakskinParticlesPacket::handle);
        payloadRegistrar.playToClient(RegenCloudParticlesPacket.TYPE, RegenCloudParticlesPacket.STREAM_CODEC, RegenCloudParticlesPacket::handle);
        payloadRegistrar.playToClient(ShockwaveParticlesPacket.TYPE, ShockwaveParticlesPacket.STREAM_CODEC, ShockwaveParticlesPacket::handle);
        payloadRegistrar.playToClient(TeleportParticlesPacket.TYPE, TeleportParticlesPacket.STREAM_CODEC, TeleportParticlesPacket::handle);

        //CASTING
        payloadRegistrar.playToClient(CastErrorPacket.TYPE, CastErrorPacket.STREAM_CODEC, CastErrorPacket::handle);
        payloadRegistrar.playToClient(OnCastFinished.TYPE, OnCastFinished.STREAM_CODEC, OnCastFinished::handle);
        payloadRegistrar.playToClient(OnCastStarted.TYPE, OnCastStarted.STREAM_CODEC, OnCastStarted::handle);
        payloadRegistrar.playToClient(OnClientCastPacket.TYPE, OnClientCastPacket.STREAM_CODEC, OnClientCastPacket::handle);
        payloadRegistrar.playToClient(RemoveRecastPacket.TYPE, RemoveRecastPacket.STREAM_CODEC, RemoveRecastPacket::handle);
        payloadRegistrar.playToClient(SyncCooldownPacket.TYPE, SyncCooldownPacket.STREAM_CODEC, SyncCooldownPacket::handle);
        payloadRegistrar.playToClient(SyncCooldownsPacket.TYPE, SyncCooldownsPacket.STREAM_CODEC, SyncCooldownsPacket::handle);
        payloadRegistrar.playToClient(SyncEntityDataPacket.TYPE, SyncEntityDataPacket.STREAM_CODEC, SyncEntityDataPacket::handle);
        payloadRegistrar.playToClient(SyncPlayerDataPacket.TYPE, SyncPlayerDataPacket.STREAM_CODEC, SyncPlayerDataPacket::handle);
        payloadRegistrar.playToClient(SyncRecastPacket.TYPE, SyncRecastPacket.STREAM_CODEC, SyncRecastPacket::handle);
        payloadRegistrar.playToClient(SyncRecastsPacket.TYPE, SyncRecastsPacket.STREAM_CODEC, SyncRecastsPacket::handle);
        payloadRegistrar.playToClient(SyncTargetingDataPacket.TYPE, SyncTargetingDataPacket.STREAM_CODEC, SyncTargetingDataPacket::handle);
        payloadRegistrar.playToClient(UpdateCastingStatePacket.TYPE, UpdateCastingStatePacket.STREAM_CODEC, UpdateCastingStatePacket::handle);

        payloadRegistrar.playToServer(CancelCastPacket.TYPE, CancelCastPacket.STREAM_CODEC, CancelCastPacket::handle);
        payloadRegistrar.playToServer(CastPacket.TYPE, CastPacket.STREAM_CODEC, CastPacket::handle);
        payloadRegistrar.playToServer(QuickCastPacket.TYPE, QuickCastPacket.STREAM_CODEC, QuickCastPacket::handle);

        //SPELLS
        payloadRegistrar.playToServer(LearnSpellPacket.TYPE, LearnSpellPacket.STREAM_CODEC, LearnSpellPacket::handle);
        payloadRegistrar.playToClient(GuidingBoltManagerStartTrackingPacket.TYPE, GuidingBoltManagerStartTrackingPacket.STREAM_CODEC, GuidingBoltManagerStartTrackingPacket::handle);
        payloadRegistrar.playToClient(GuidingBoltManagerStopTrackingPacket.TYPE, GuidingBoltManagerStopTrackingPacket.STREAM_CODEC, GuidingBoltManagerStopTrackingPacket::handle);

    }
}


