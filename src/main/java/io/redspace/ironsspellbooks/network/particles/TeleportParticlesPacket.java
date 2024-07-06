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

public class TeleportParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos1;
    private final Vec3 pos2;
    public static final CustomPacketPayload.Type<TeleportParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "teleport_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(TeleportParticlesPacket::write, TeleportParticlesPacket::new);

    public TeleportParticlesPacket(Vec3 pos1, Vec3 pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public TeleportParticlesPacket(FriendlyByteBuf buf) {
        pos1 = buf.readVec3();
        pos2 = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos1);
        buf.writeVec3(pos2);
    }

    public static void handle(TeleportParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientboundTeleport(packet.pos1, packet.pos2);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
