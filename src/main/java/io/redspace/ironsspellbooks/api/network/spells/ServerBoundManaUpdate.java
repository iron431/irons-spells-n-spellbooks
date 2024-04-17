package io.redspace.ironsspellbooks.api.network.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.server.level.ServerPlayer;

public class ServerBoundManaUpdate {
    public ServerBoundManaUpdate(ServerPlayer serverPlayer, MagicData magicData){
        Messages.sendToPlayer(new ClientboundSyncMana(magicData),serverPlayer);
    }
}
