package io.redspace.ironsspellbooks.spells;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
    public static final StreamCodec<RegistryFriendlyByteBuf, FireWallCastComponent> FIRE_WALL_CAST_DATA = StreamCodec.of(
            (buf, data) -> {
                buf.writeFloat(data.maxTotalDistance);
                buf.writeFloat(data.accumulatedDistance);
                buf.writeVarInt(data.anchorPoints.size());
                for (Vec3 vec : data.anchorPoints) {
                    buf.writeFloat((float) vec.x);
                    buf.writeFloat((float) vec.y);
                    buf.writeFloat((float) vec.z);
                }
            },
            buf -> {
                FireWallCastComponent data = new FireWallCastComponent(buf.readFloat());
                data.accumulatedDistance = buf.readFloat();
                int length = buf.readVarInt();
                for (int i = 0; i < length; i++) {
                    data.anchorPoints.add(new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat()));
                }
                return data;
            });

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
