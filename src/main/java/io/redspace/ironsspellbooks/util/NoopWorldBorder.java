package io.redspace.ironsspellbooks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a No-op World Border that does nothing
 */
public class NoopWorldBorder extends WorldBorder {
    public boolean isWithinBounds(double x, double z, double offset) {
        return true;
    }

    @Override
    public @NotNull BlockPos clampToBounds(double x, double y, double z) {
        return BlockPos.containing(x, y, z);
    }

    @Override
    public double getDistanceToBorder(double x, double z) {
        return MAX_SIZE;
    }

    @Override
    public void setSize(double size) {
        return;
    }

    @Override
    public void setCenter(double x, double z) {
        return;
    }
}
