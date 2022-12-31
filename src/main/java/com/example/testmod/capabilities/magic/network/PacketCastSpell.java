package com.example.testmod.capabilities.magic.network;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCastSpell {

    private final int spellId;
    private final int duration;
    private final int playerMana;

    public PacketCastSpell(int spellID, int duration, int playerMana) {
        this.spellId = spellID;
        this.duration = duration;
        this.playerMana = playerMana;
    }

    public PacketCastSpell(FriendlyByteBuf buf) {
        this.spellId = buf.readInt();
        this.duration = buf.readInt();
        this.playerMana = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellId);
        buf.writeInt(this.duration);
        buf.writeInt(this.playerMana);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TestMod.LOGGER.info("PacketCastSpell:" + spellId + "," + duration);
            ClientMagicData.setMana(playerMana);
            ClientMagicData.getCooldowns().addCooldown(SpellType.values()[spellId], duration);
            ClientMagicData.isCasting = false;
            ClientMagicData.castDuration = 0;
            ClientMagicData.castDurationRemaining = 0;
        });
        return true;
    }
}
