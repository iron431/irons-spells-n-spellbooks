package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

public class SoulfireRayParticleOptions implements ParticleOptions {
    public static StreamCodec<? super ByteBuf, SoulfireRayParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeDouble(option.destination.x);
                buf.writeDouble(option.destination.y);
                buf.writeDouble(option.destination.z);
            },
            (buf) -> new SoulfireRayParticleOptions(buf.readDouble() , buf.readDouble(), buf.readDouble())
    );

    public static MapCodec<SoulfireRayParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.INT_STREAM.fieldOf("destination").forGetter((option) -> IntStream.of((int) option.destination.x * 10, (int) option.destination.y * 10, (int) option.destination.z * 10))
            ).apply(object, (stream) -> {
                        var array = stream.toArray();
                        return new SoulfireRayParticleOptions(new Vec3(array[0] , array[1], array[2] ));
                    }
            ));

    private final Vec3 destination;

    public SoulfireRayParticleOptions(Vec3 destination) {
        this.destination = destination;
    }

    public SoulfireRayParticleOptions(double x, double y, double z) {
        this(new Vec3(x, y, z));
    }

    public ParticleType<SoulfireRayParticleOptions> getType() {
        return ParticleRegistry.SOULFIRE_RAY_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

}
