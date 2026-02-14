package io.redspace.ironsspellbooks.patreon;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.command.IronsDebugCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PatreonHandler {
    public static PatreonPermissions getPatreonPermissions(@NotNull Player player) {
        return getPatreonPermissions(player.getUUID());
    }

    public static PatreonPermissions getPatreonPermissions(UUID playerUUID) {
        //todo: implement
        return PatreonPermissions.None;
    }

    public static PatreonPermissions getPatreonPermissionsByUsername(String username) {
        //todo: implement
        return PatreonPermissions.None;
    }

    public static @Nullable UUID profileFromUsername(String userName) {
        //todo: lookup via patreon data. do literal http lookup for now for prototyping
        return IronsDebugCommand.getProfileByUsername(Minecraft.getInstance().getSingleplayerServer(), userName).map(GameProfile::getId).orElse(null);
    }
}
