package io.redspace.ironsspellbooks.network.ported.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class GuidingBoltManagerStopTrackingPacket implements CustomPacketPayload {
    private final UUID entity;
    public static final CustomPacketPayload.Type<GuidingBoltManagerStopTrackingPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "guiding_bolt_manager_stop_tracking"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuidingBoltManagerStopTrackingPacket> STREAM_CODEC = CustomPacketPayload.codec(GuidingBoltManagerStopTrackingPacket::write, GuidingBoltManagerStopTrackingPacket::new);

    public GuidingBoltManagerStopTrackingPacket(Entity entity) {
        this.entity = entity.getUUID();
    }

    public GuidingBoltManagerStopTrackingPacket(FriendlyByteBuf buf) {
        this.entity = buf.readUUID();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(entity);
    }

    public static void handle(GuidingBoltManagerStopTrackingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            GuidingBoltManager.handleClientboundStopTracking(packet.entity);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}