package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ScrollForgeSelectSpellPacket implements CustomPacketPayload {
    private final BlockPos pos;
    private final String spellId;
    public static final CustomPacketPayload.Type<ScrollForgeSelectSpellPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "scroll_forge_select_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollForgeSelectSpellPacket> STREAM_CODEC = CustomPacketPayload.codec(ScrollForgeSelectSpellPacket::write, ScrollForgeSelectSpellPacket::new);

    public ScrollForgeSelectSpellPacket(BlockPos pos, String spellId) {
        this.pos = pos;
        this.spellId = spellId;
    }

    public ScrollForgeSelectSpellPacket(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        spellId = buf.readUtf();

    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeUtf(spellId);
    }

    public static void handle(ScrollForgeSelectSpellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ScrollForgeTile scrollForgeTile = (ScrollForgeTile) context.player().level().getBlockEntity(packet.pos);
            if (scrollForgeTile != null) {
                scrollForgeTile.setRecipeSpell(packet.spellId);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
