package io.redspace.ironsspellbooks.network.ported.particles;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class FortifyAreaParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos;
    public static final CustomPacketPayload.Type<FortifyAreaParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fortify_area_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FortifyAreaParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(FortifyAreaParticlesPacket::write, FortifyAreaParticlesPacket::new);

    public FortifyAreaParticlesPacket(Vec3 pos) {
        this.pos = pos;
    }

    public FortifyAreaParticlesPacket(FriendlyByteBuf buf) {
        pos = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos);
    }

    public static void handle(FortifyAreaParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientsideFortifyAreaParticles(packet.pos);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
