package com.example.testmod.network;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncManaToClient {

    private int playerMana = 0;
    private PlayerMagicData playerMagicData = null;


    public PacketSyncManaToClient(PlayerMagicData playerMagicData) {
        //Server side only
        this.playerMagicData = playerMagicData;
    }

    public PacketSyncManaToClient(FriendlyByteBuf buf) {
        playerMana = buf.readInt();
//        int numCooldowns = buf.readInt();
//        for (int i = 0; i < numCooldowns; i++) {
//            SpellType spellType = SpellType.values()[buf.readInt()];
//            int spellCooldown = buf.readInt();
//            int cooldownRemaining = buf.readInt();
//            this.spellCooldowns.put(spellType, new CooldownInstance(spellCooldown, cooldownRemaining));
//        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerMagicData.getMana());
//        if (playerMagicData != null) {
//            var cooldowns = playerMagicData.getPlayerCooldowns().getSpellCooldowns();
//            buf.writeInt(cooldowns.size());
//            cooldowns.forEach((k, v) -> {
//                buf.writeInt(k.getValue());
//                buf.writeInt(v.getSpellCooldown());
//                buf.writeInt(v.getCooldownRemaining());
//            });
//        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.setMana(playerMana);
            //ClientMagicData.setCooldowns(spellCooldowns);
        });
        return true;
    }
}
