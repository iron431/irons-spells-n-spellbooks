package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.network.AddMotionToPlayerPacket;
import io.redspace.ironsspellbooks.network.EntityEventPacket;
import io.redspace.ironsspellbooks.network.OpenEldritchScreenPacket;
import io.redspace.ironsspellbooks.network.ScrollForgeSelectSpellPacket;
import io.redspace.ironsspellbooks.network.SyncAllCameraShakesPacket;
import io.redspace.ironsspellbooks.network.SyncAnimationPacket;
import io.redspace.ironsspellbooks.network.SyncCameraShakePacket;
import io.redspace.ironsspellbooks.network.SyncJsonConfigPacket;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.network.particles.AbsorptionParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.BloodSiphonParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FlamethrowerParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FortifyAreaParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FrostStepParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.HealParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.OakskinParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.RegenCloudParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.network.spells.GuidingBoltManagerStartTrackingPacket;
import io.redspace.ironsspellbooks.network.spells.GuidingBoltManagerStopTrackingPacket;
import io.redspace.ironsspellbooks.network.spells.LearnSpellPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IronsSpellbooks.MODID)
public class PayloadHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(IronsSpellbooks.MODID).versioned("1.0.0").optional();

        //GENERAL
        payloadRegistrar.playToClient(AddMotionToPlayerPacket.TYPE, AddMotionToPlayerPacket.STREAM_CODEC, AddMotionToPlayerPacket::handle);
        payloadRegistrar.playToClient(EntityEventPacket.TYPE, EntityEventPacket.STREAM_CODEC, EntityEventPacket::handle);
        payloadRegistrar.playToClient(OpenEldritchScreenPacket.TYPE, OpenEldritchScreenPacket.STREAM_CODEC, OpenEldritchScreenPacket::handle);
        payloadRegistrar.playToClient(SyncAnimationPacket.TYPE, SyncAnimationPacket.STREAM_CODEC, SyncAnimationPacket::handle);
        payloadRegistrar.playToClient(SyncCameraShakePacket.TYPE, SyncCameraShakePacket.STREAM_CODEC, SyncCameraShakePacket::handle);
        payloadRegistrar.playToClient(SyncAllCameraShakesPacket.TYPE, SyncAllCameraShakesPacket.STREAM_CODEC, SyncAllCameraShakesPacket::handle);
        payloadRegistrar.playToClient(SyncManaPacket.TYPE, SyncManaPacket.STREAM_CODEC, SyncManaPacket::handle);
        payloadRegistrar.playToClient(PlayPlayerAnimationPacket.TYPE, PlayPlayerAnimationPacket.STREAM_CODEC, PlayPlayerAnimationPacket::handle);

        payloadRegistrar.playToServer(ScrollForgeSelectSpellPacket.TYPE, ScrollForgeSelectSpellPacket.STREAM_CODEC, ScrollForgeSelectSpellPacket::handle);
        payloadRegistrar.playToClient(SyncJsonConfigPacket.TYPE, SyncJsonConfigPacket.STREAM_CODEC, SyncJsonConfigPacket::handle);

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
        payloadRegistrar.playToClient(FlamethrowerParticlesPacket.TYPE, FlamethrowerParticlesPacket.STREAM_CODEC, FlamethrowerParticlesPacket::handle);

        //SPELLS
        payloadRegistrar.playToClient(GuidingBoltManagerStartTrackingPacket.TYPE, GuidingBoltManagerStartTrackingPacket.STREAM_CODEC, GuidingBoltManagerStartTrackingPacket::handle);
        payloadRegistrar.playToClient(GuidingBoltManagerStopTrackingPacket.TYPE, GuidingBoltManagerStopTrackingPacket.STREAM_CODEC, GuidingBoltManagerStopTrackingPacket::handle);

        payloadRegistrar.playToServer(LearnSpellPacket.TYPE, LearnSpellPacket.STREAM_CODEC, LearnSpellPacket::handle);
    }
}


