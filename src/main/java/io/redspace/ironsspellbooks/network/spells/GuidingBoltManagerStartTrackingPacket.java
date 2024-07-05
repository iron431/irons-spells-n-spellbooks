package io.redspace.ironsspellbooks.network.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuidingBoltManagerStartTrackingPacket implements CustomPacketPayload {

    private final UUID entity;
    private final List<Integer> projectileIds;

    public static final CustomPacketPayload.Type<GuidingBoltManagerStartTrackingPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "guiding_bolt_manager_start_tracking"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuidingBoltManagerStartTrackingPacket> STREAM_CODEC = CustomPacketPayload.codec(GuidingBoltManagerStartTrackingPacket::write, GuidingBoltManagerStartTrackingPacket::new);

    public GuidingBoltManagerStartTrackingPacket(Entity entity, List<Projectile> projectiles) {
        this.entity = entity.getUUID();
        this.projectileIds = projectiles.stream().map(Entity::getId).toList();
    }

    public GuidingBoltManagerStartTrackingPacket(FriendlyByteBuf buf) {
        projectileIds = new ArrayList<>();
        this.entity = buf.readUUID();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            projectileIds.add(buf.readInt());
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(entity);
        buf.writeInt(projectileIds.size());
        for (Integer projectileId : projectileIds) {
            buf.writeInt(projectileId);
        }
    }

    public static void handle(GuidingBoltManagerStartTrackingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            GuidingBoltManager.handleClientboundStartTracking(packet.entity, packet.projectileIds);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}