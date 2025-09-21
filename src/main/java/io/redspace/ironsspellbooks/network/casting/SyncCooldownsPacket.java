package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class SyncCooldownsPacket implements CustomPacketPayload {
    private final Map<String, CooldownInstance> spellCooldowns;

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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeMap(spellCooldowns, SyncCooldownsPacket::writeSpellId, SyncCooldownsPacket::writeCoolDownInstance);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var cooldowns = ClientMagicData.getCooldowns();
            cooldowns.clearCooldowns();
            this.spellCooldowns.forEach((k, v) -> {
                //irons_spellbooks.LOGGER.debug("ClientboundSyncCooldowns {} {} {}", k, v.getSpellCooldown(), v.getCooldownRemaining());
                cooldowns.addCooldown(k, v.getSpellCooldown(), v.getCooldownRemaining());
            });
            ClientMagicData.resetClientCastState(null);
        });
        return true;
    }
}