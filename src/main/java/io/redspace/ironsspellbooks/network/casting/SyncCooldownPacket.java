package io.redspace.ironsspellbooks.network.ported.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncCooldownPacket implements CustomPacketPayload {
    private final String spellId;
    private final int duration;
    public static final CustomPacketPayload.Type<SyncCooldownPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_cooldown"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldownPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncCooldownPacket::write, SyncCooldownPacket::new);

    public SyncCooldownPacket(String spellId, int duration) {
        this.spellId = spellId;
        this.duration = duration;
    }

    public SyncCooldownPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        duration = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(duration);
    }

    public static void handle(SyncCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientMagicData.getCooldowns().addCooldown(packet.spellId, packet.duration);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
