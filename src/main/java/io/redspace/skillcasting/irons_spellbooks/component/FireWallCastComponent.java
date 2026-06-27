package io.redspace.skillcasting.irons_spellbooks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class FireWallCastComponent {
    public static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.DOUBLE.fieldOf("x").forGetter(v -> v.x),
            Codec.DOUBLE.fieldOf("y").forGetter(v -> v.y),
            Codec.DOUBLE.fieldOf("z").forGetter(v -> v.z)
    ).apply(builder, Vec3::new));

    public static final Codec<FireWallCastComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(VEC3_CODEC).fieldOf("anchors").forGetter(c -> c.anchorPoints),
            Codec.FLOAT.fieldOf("max_distance").forGetter(c -> c.maxTotalDistance),
            Codec.FLOAT.fieldOf("accumulated").forGetter(c -> c.accumulatedDistance)
    ).apply(builder, FireWallCastComponent::fromCodec));

    public final List<Vec3> anchorPoints = new ArrayList<>();
    public float maxTotalDistance;
    public float accumulatedDistance;

    public FireWallCastComponent(float maxTotalDistance) {
        this.maxTotalDistance = maxTotalDistance;
    }

    private FireWallCastComponent(List<Vec3> anchorPoints, float maxTotalDistance, float accumulatedDistance) {
        this.anchorPoints.addAll(anchorPoints);
        this.maxTotalDistance = maxTotalDistance;
        this.accumulatedDistance = accumulatedDistance;
    }

    private static FireWallCastComponent fromCodec(List<Vec3> anchorPoints, float maxTotalDistance, float accumulatedDistance) {
        return new FireWallCastComponent(anchorPoints, maxTotalDistance, accumulatedDistance);
    }
}
