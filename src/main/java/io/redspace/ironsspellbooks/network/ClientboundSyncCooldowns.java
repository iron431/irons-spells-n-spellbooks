package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ClientboundSyncCooldowns {
    private final Map<SpellType, CooldownInstance> spellCooldowns;

    public static SpellType readSpellType(FriendlyByteBuf buffer) {
        return SpellType.getTypeFromValue(buffer.readInt());
    }

    public static CooldownInstance readCoolDownInstance(FriendlyByteBuf buffer) {
        int spellCooldown = buffer.readInt();
        int spellCooldownRemaining = buffer.readInt();
        return new CooldownInstance(spellCooldown, spellCooldownRemaining);
    }

    public static void writeSpellType(FriendlyByteBuf buf, SpellType spellType) {
        buf.writeInt(spellType.getValue());
    }

    public static void writeCoolDownInstance(FriendlyByteBuf buf, CooldownInstance cooldownInstance) {
        buf.writeInt(cooldownInstance.getSpellCooldown());
        buf.writeInt(cooldownInstance.getCooldownRemaining());
    }

    public ClientboundSyncCooldowns(Map<SpellType, CooldownInstance> spellCooldowns) {
        this.spellCooldowns = spellCooldowns;
    }

    public ClientboundSyncCooldowns(FriendlyByteBuf buf) {
        this.spellCooldowns = buf.readMap(ClientboundSyncCooldowns::readSpellType, ClientboundSyncCooldowns::readCoolDownInstance);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeMap(spellCooldowns, ClientboundSyncCooldowns::writeSpellType, ClientboundSyncCooldowns::writeCoolDownInstance);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            this.spellCooldowns.forEach((k, v) -> {
                //irons_spellbooks.LOGGER.debug("ClientboundSyncCooldowns {} {} {}", k, v.getSpellCooldown(), v.getCooldownRemaining());
                ClientMagicData.getCooldowns().addCooldown(k, v.getSpellCooldown(), v.getCooldownRemaining());
            });
            ClientMagicData.resetClientCastState();

        });
        return true;
    }
}