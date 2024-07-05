package io.redspace.ironsspellbooks.network.ported.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class SyncCooldownsPacket implements CustomPacketPayload {
    private final Map<String, CooldownInstance> spellCooldowns;
    public static final CustomPacketPayload.Type<SyncCooldownsPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_cooldowns"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldownsPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncCooldownsPacket::write, SyncCooldownsPacket::new);

    public static String readSpellID(FriendlyByteBuf buffer) {
        return buffer.readUtf();
    }

    public static CooldownInstance readCoolDownInstance(FriendlyByteBuf buffer) {
        int spellCooldown = buffer.readInt();
        int spellCooldownRemaining = buffer.readInt();
        return new CooldownInstance(spellCooldown, spellCooldownRemaining);
    }

    public static void writeSpellId(FriendlyByteBuf buf, String spellId) {
        buf.writeUtf(spellId);
    }

    public static void writeCoolDownInstance(FriendlyByteBuf buf, CooldownInstance cooldownInstance) {
        buf.writeInt(cooldownInstance.getSpellCooldown());
        buf.writeInt(cooldownInstance.getCooldownRemaining());
    }

    public SyncCooldownsPacket(Map<String, CooldownInstance> spellCooldowns) {
        this.spellCooldowns = spellCooldowns;
    }

    public SyncCooldownsPacket(FriendlyByteBuf buf) {
        this.spellCooldowns = buf.readMap(SyncCooldownsPacket::readSpellID, SyncCooldownsPacket::readCoolDownInstance);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeMap(spellCooldowns, SyncCooldownsPacket::writeSpellId, SyncCooldownsPacket::writeCoolDownInstance);
    }

    public static void handle(SyncCooldownsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var cooldowns = ClientMagicData.getCooldowns();
            cooldowns.clearCooldowns();
            packet.spellCooldowns.forEach((k, v) -> {
                cooldowns.addCooldown(k, v.getSpellCooldown(), v.getCooldownRemaining());
            });
            ClientMagicData.resetClientCastState(null);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}