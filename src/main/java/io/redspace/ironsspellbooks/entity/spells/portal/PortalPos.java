package io.redspace.ironsspellbooks.entity.spells.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalPos {
    private final ResourceKey<Level> dimension;
    private final Vec3 pos;
    /**
     * degrees
     */
    private final float rotation;

    private PortalPos(ResourceKey<Level> dimension, Vec3 pos, float rotation) {
        this.dimension = dimension;
        this.pos = pos;
        this.rotation = rotation;
    }

    public static PortalPos of(ResourceKey<Level> dimension, Vec3 pos, float rotation) {
        return new PortalPos(dimension, pos, rotation);
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public Vec3 pos() {
        return this.pos;
    }

    /**
     * degrees
     */
    public float rotation() {
        return rotation;
    }
}
