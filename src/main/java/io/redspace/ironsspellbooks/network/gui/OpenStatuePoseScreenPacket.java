package io.redspace.ironsspellbooks.network.gui;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.statue.StatueBlockEntity;
import io.redspace.ironsspellbooks.block.statue.StatuePoseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenStatuePoseScreenPacket implements CustomPacketPayload {
    private final BlockPos blockPos;
    public static final Type<OpenStatuePoseScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "open_player_statue_pose_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenStatuePoseScreenPacket> STREAM_CODEC = CustomPacketPayload.codec(OpenStatuePoseScreenPacket::write, OpenStatuePoseScreenPacket::new);

    public OpenStatuePoseScreenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public OpenStatuePoseScreenPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    public static void handle(OpenStatuePoseScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getBlockEntity(packet.blockPos) instanceof StatueBlockEntity statueBlock) {
                Minecraft.getInstance().setScreen(new StatuePoseScreen(packet.blockPos, statueBlock.getStatueData()));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
