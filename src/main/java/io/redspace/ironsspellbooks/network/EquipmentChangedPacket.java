package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class EquipmentChangedPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EquipmentChangedPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "equipment_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentChangedPacket> STREAM_CODEC = CustomPacketPayload.codec(EquipmentChangedPacket::write, EquipmentChangedPacket::new);

    public EquipmentChangedPacket() {
    }

    public EquipmentChangedPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(true);
    }

    public static void handle(EquipmentChangedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientMagicData.updateSpellSelectionManager());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}