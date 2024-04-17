package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.server.level.ServerPlayer;

public class UpdateClient {
    // More Util updates to be added
    public static void SendManaUpdate(ServerPlayer serverPlayer, MagicData magicData){
        Messages.sendToPlayer(new ClientboundSyncMana(magicData),serverPlayer);
    }


}
