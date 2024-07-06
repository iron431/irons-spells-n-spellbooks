package io.redspace.ironsspellbooks.network.particles;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RegenCloudParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos;
    public static final CustomPacketPayload.Type<RegenCloudParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "regen_cloud_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RegenCloudParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(RegenCloudParticlesPacket::write, RegenCloudParticlesPacket::new);

    public RegenCloudParticlesPacket(Vec3 pos) {
        this.pos = pos;
    }

    public RegenCloudParticlesPacket(FriendlyByteBuf buf) {
        pos = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos);
    }

    public static void handle(RegenCloudParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientsideRegenCloudParticles(packet.pos);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
