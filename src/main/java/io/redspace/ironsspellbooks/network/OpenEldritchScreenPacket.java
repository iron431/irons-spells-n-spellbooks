package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenEldritchScreenPacket implements CustomPacketPayload {
    private final InteractionHand hand;
    public static final CustomPacketPayload.Type<OpenEldritchScreenPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "open_eldritch_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEldritchScreenPacket> STREAM_CODEC = CustomPacketPayload.codec(OpenEldritchScreenPacket::write, OpenEldritchScreenPacket::new);

    public OpenEldritchScreenPacket(InteractionHand pHand) {
        this.hand = pHand;
    }

    public OpenEldritchScreenPacket(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
    }

    public static void handle(OpenEldritchScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.openEldritchResearchScreen(packet.hand);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}