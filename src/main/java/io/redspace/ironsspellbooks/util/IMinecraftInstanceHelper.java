package io.redspace.ironsspellbooks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IMinecraftInstanceHelper {
    @Nullable
    Player player();

    default void openStatueScreen(BlockPos blockPos){

    }
}
