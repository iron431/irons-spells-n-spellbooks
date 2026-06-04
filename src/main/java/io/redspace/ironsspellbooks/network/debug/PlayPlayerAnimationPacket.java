package io.redspace.ironsspellbooks.network.debug;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PlayPlayerAnimationPacket implements CustomPacketPayload {
    public static final Type<PlayPlayerAnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "play_player_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayPlayerAnimationPacket> STREAM_CODEC = CustomPacketPayload.codec(PlayPlayerAnimationPacket::write, PlayPlayerAnimationPacket::new);

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

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeResourceLocation(animation);
    }

    public static void handle(PlayPlayerAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var player = level.getPlayerByUUID(packet.playerId);
            if (player != null) {
                AnimationHelper.animatePlayerStart(player, packet.animation);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
