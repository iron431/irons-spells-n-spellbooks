package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.animation.IAnimatedAttacker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncAnimationPacket<T extends Entity & IAnimatedAttacker> implements CustomPacketPayload {
    int entityId;
    String animationId;
    public static final CustomPacketPayload.Type<SyncAnimationPacket<?>> TYPE = new CustomPacketPayload.Type<>(Skillcasting.id("sync_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAnimationPacket<?>> STREAM_CODEC = CustomPacketPayload.codec(SyncAnimationPacket::write, SyncAnimationPacket::new);

    public SyncAnimationPacket(String animationId, T entity) {
        this.entityId = entity.getId();
        this.animationId = animationId;
    }

    public SyncAnimationPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        animationId = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(animationId);
    }

    public static void handle(SyncAnimationPacket<?> packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var entity = level.getEntity(packet.entityId);
            if (entity instanceof IAnimatedAttacker animatedAttacker) {
                animatedAttacker.playAnimation(packet.animationId);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
