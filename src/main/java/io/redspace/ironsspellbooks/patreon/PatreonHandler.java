package io.redspace.ironsspellbooks.patreon;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PatreonHandler {
    public static PatreonPermissions getPatreonPermissions(@NotNull Player player) {
        return getPatreonPermissions(player.getUUID());
    }

    public static PatreonPermissions getPatreonPermissions(UUID playerUUID) {
        //todo: implement
        return PatreonPermissions.None;
    }
}
