package io.redspace.ironsspellbooks.network.mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.dragon.DragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DragonSyncWalkStatePacket implements CustomPacketPayload {
    private final int entityId;
    private final float position;
    private final float speed;
    public static final Type<DragonSyncWalkStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_walk_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonSyncWalkStatePacket> STREAM_CODEC = CustomPacketPayload.codec(DragonSyncWalkStatePacket::write, DragonSyncWalkStatePacket::new);

    public DragonSyncWalkStatePacket(DragonEntity livingEntity) {
        this.entityId = livingEntity.getId();
        this.position = livingEntity.walkAnimation.position();
        this.speed = livingEntity.walkAnimation.speed();
    }

    public DragonSyncWalkStatePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.position = buf.readFloat();
        this.speed = buf.readFloat();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(position);
        buf.writeFloat(speed);
    }

    public static void handle(DragonSyncWalkStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if(level != null){
                var entity = level.getEntity(packet.entityId);;
                if(entity instanceof DragonEntity livingEntity){
                    livingEntity.walkAnimation.setSpeed(packet.speed);
                    livingEntity.walkAnimation.position = packet.position;
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
