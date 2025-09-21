package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SyncAnimationPacket<T extends Entity & IAnimatedAttacker> implements CustomPacketPayload {
    int entityId;
    String animationId;

    public SyncAnimationPacket(String animationId, T entity) {
        this.entityId = entity.getId();
        this.animationId = animationId;
    }

    public SyncAnimationPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        animationId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(animationId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var entity = level.getEntity(entityId);
            if (entity instanceof IAnimatedAttacker animatedAttacker) {
                animatedAttacker.playAnimation(animationId);
            }
        });

        return true;
    }
}
