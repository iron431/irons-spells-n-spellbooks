package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class QuickCastPacket implements CustomPacketPayload {

    private int slot;
    public static final CustomPacketPayload.Type<QuickCastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "quick_cast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickCastPacket> STREAM_CODEC = CustomPacketPayload.codec(QuickCastPacket::write, QuickCastPacket::new);

    public QuickCastPacket(int slot) {
        this.slot = slot;
    }

    public QuickCastPacket(FriendlyByteBuf buf) {
        slot = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public static void handle(QuickCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Utils.serverSideInitiateQuickCast(serverPlayer, packet.slot);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
