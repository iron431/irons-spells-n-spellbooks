package io.redspace.ironsspellbooks.network.gui;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.statue.StatueBlockEntity;
import io.redspace.ironsspellbooks.patreon.statue.PlayerStatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SelectStatuePosePacket implements CustomPacketPayload {
    private final BlockPos blockPos;
    private final PlayerStatuePose pose;
    private final boolean flipped;
    public static final Type<SelectStatuePosePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "select_player_statue_pose"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectStatuePosePacket> STREAM_CODEC = CustomPacketPayload.codec(SelectStatuePosePacket::write, SelectStatuePosePacket::new);

    public SelectStatuePosePacket(BlockPos blockPos, PlayerStatuePose pose, boolean flipped) {
        this.blockPos = blockPos;
        this.pose = pose;
        this.flipped = flipped;
    }

    public SelectStatuePosePacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.pose = PlayerStatuePose.fromString(buf.readUtf());
        this.flipped = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeUtf(pose.getSerializedName());
        buf.writeBoolean(this.flipped);
    }

    public static void handle(SelectStatuePosePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.blockPos) instanceof StatueBlockEntity statueBlock) {
                statueBlock.getPrimaryControllerOpt().ifPresent(
                        controller -> {
                            statueBlock.setPose(packet.pose);
                            statueBlock.setFlipped(packet.flipped);
                        }
                );
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
