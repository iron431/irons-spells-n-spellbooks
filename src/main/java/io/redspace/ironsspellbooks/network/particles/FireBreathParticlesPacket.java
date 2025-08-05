package io.redspace.ironsspellbooks.network.particles;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.Vec3StreamCodec;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FireBreathParticlesPacket(Vec3 pos, Vec3 dir) implements CustomPacketPayload {
    public static final Type<FireBreathParticlesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fire_breath_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FireBreathParticlesPacket> STREAM_CODEC = StreamCodec.composite(
            Vec3StreamCodec.CODEC, FireBreathParticlesPacket::pos,
            Vec3StreamCodec.CODEC, FireBreathParticlesPacket::dir,
            FireBreathParticlesPacket::new
    );

    public static void handle(FireBreathParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level;
            double x = packet.pos.x;
            double y = packet.pos.y;
            double z = packet.pos.z;

            double speed = Utils.random.nextDouble() * .35 + .35;
            for (int i = 0; i < 10; i++) {
                double offset = .15;
                double ox = Math.random() * 2 * offset - offset;
                double oy = Math.random() * 2 * offset - offset;
                double oz = Math.random() * 2 * offset - offset;

                double angularness = .5;
                Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
                Vec3 result = (packet.dir.scale(3).add(randomVec)).normalize().scale(speed);
                level.addParticle(ParticleHelper.FIRE_EMITTER, x + ox, y + oy, z + oz, result.x, result.y, result.z);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
