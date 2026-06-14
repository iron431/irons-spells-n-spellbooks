package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalPos {

    public static final Codec<PortalPos> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(PortalPos::dimension),
            Vec3.CODEC.fieldOf("pos").forGetter(PortalPos::pos),
            Codec.FLOAT.fieldOf("rotation").forGetter(PortalPos::rotation)
    ).apply(builder, PortalPos::new));

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

    @Override
    public String toString() {
        return "PortalPos{" + this.dimension + " " + this.pos + "}";
    }
}
