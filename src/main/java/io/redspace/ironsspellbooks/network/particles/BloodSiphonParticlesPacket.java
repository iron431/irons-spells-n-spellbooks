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

public class BloodSiphonParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos1;
    private final Vec3 pos2;
    public static final CustomPacketPayload.Type<BloodSiphonParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "blood_siphon_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodSiphonParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(BloodSiphonParticlesPacket::write, BloodSiphonParticlesPacket::new);

    public BloodSiphonParticlesPacket(Vec3 pos1, Vec3 pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public BloodSiphonParticlesPacket(FriendlyByteBuf buf) {
        pos1 = buf.readVec3();
        pos2 = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos1);
        buf.writeVec3(pos2);
    }

    public static void handle(BloodSiphonParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientboundBloodSiphonParticles(packet.pos1, packet.pos2);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
