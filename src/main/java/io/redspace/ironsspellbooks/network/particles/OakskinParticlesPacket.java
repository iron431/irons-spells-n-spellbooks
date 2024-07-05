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

public class OakskinParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos;
    public static final CustomPacketPayload.Type<OakskinParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "oakskin_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OakskinParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(OakskinParticlesPacket::write, OakskinParticlesPacket::new);

    public OakskinParticlesPacket(Vec3 pos) {
        this.pos = pos;
    }

    public OakskinParticlesPacket(FriendlyByteBuf buf) {
        pos = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos);
    }

    public static void handle(OakskinParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientboundOakskinParticles(packet.pos);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
