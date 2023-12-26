package io.redspace.ironsspellbooks.util;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinecraftInstanceHelper implements IMinecraftInstanceHelper {
    /**
     * If we are on the client, this is replaced with an implementation that returns the client host player
     */
    public static IMinecraftInstanceHelper instance = () -> null;

    @Nullable
    @Override
    public Player player() {
        return instance.player();
    }

    @Nullable
    public static Player getPlayer() {
        return instance.player();
    }

    public static void ifPlayerPresent(Consumer<Player> consumer) {
        var player = getPlayer();
        if (player != null) {
            consumer.accept(player);
        }
    }
}
