package io.redspace.ironsspellbooks.network.debug;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayPlayerAnimationPacket implements CustomPacketPayload {
    private final UUID playerId;
    private final ResourceLocation animation;

    public PlayPlayerAnimationPacket(UUID playerId, ResourceLocation animation) {
        this.playerId = playerId;
        this.animation = animation;
    }

    public PlayPlayerAnimationPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.animation = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeResourceLocation(animation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var player = level.getPlayerByUUID(playerId);
            if (player != null) {
                AnimationHelper.animatePlayerStart(player, animation);
            }
        });
        return true;
    }
}
