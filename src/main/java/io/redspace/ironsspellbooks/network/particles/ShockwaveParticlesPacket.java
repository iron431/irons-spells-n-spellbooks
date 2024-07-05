package io.redspace.ironsspellbooks.network.ported.particles;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.function.Supplier;

public class ShockwaveParticlesPacket implements CustomPacketPayload {
    Vec3 pos;
    float radius;
    String particleName;
    public static final CustomPacketPayload.Type<ShockwaveParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "shockwave_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShockwaveParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(ShockwaveParticlesPacket::write, ShockwaveParticlesPacket::new);

    public ShockwaveParticlesPacket(Vec3 pos, float radius, ParticleType particleType) {
        this.pos = pos;
        this.radius = radius;
        this.particleName = Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType)).toString();
    }

    public ShockwaveParticlesPacket(FriendlyByteBuf buf) {
        this.pos = buf.readVec3();
        this.radius = buf.readFloat();
        this.particleName = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos);
        buf.writeFloat(radius);
        buf.writeUtf(particleName);
    }

    public static void handle(ShockwaveParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                var type = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(IronsSpellbooks.MODID, packet.particleName));
                ClientSpellCastHelper.handleClientboundShockwaveParticle(packet.pos, packet.radius, type);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
