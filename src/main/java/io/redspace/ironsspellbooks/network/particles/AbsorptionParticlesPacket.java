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

public class AbsorptionParticlesPacket implements CustomPacketPayload {
    private final Vec3 pos;
    public static final CustomPacketPayload.Type<AbsorptionParticlesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "absorption_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbsorptionParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(AbsorptionParticlesPacket::write, AbsorptionParticlesPacket::new);

    public AbsorptionParticlesPacket(Vec3 pos) {
        this.pos = pos;
    }

    public AbsorptionParticlesPacket(FriendlyByteBuf buf) {
        pos = buf.readVec3();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVec3(pos);
    }

    public static void handle(AbsorptionParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientsideAbsorptionParticles(packet.pos);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
