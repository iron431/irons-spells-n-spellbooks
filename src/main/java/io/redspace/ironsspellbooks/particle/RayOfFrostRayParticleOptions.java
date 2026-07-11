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

public class RayOfFrostRayParticleOptions implements ParticleOptions {
    public static StreamCodec<? super ByteBuf, RayOfFrostRayParticleOptions> streamCodec(ParticleType<RayOfFrostRayParticleOptions> particleType) {
        return StreamCodec.of(
                (buf, option) -> {
                    buf.writeDouble(option.destination.x);
                    buf.writeDouble(option.destination.y);
                    buf.writeDouble(option.destination.z);
                },
                (buf) -> new RayOfFrostRayParticleOptions(particleType, new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()))
        );
    }

    public static MapCodec<RayOfFrostRayParticleOptions> codec(ParticleType<RayOfFrostRayParticleOptions> particleType) {
        return RecordCodecBuilder.mapCodec(object ->
                object.group(
                        Codec.INT_STREAM.fieldOf("destination").forGetter((option) -> IntStream.of((int) option.destination.x * 10, (int) option.destination.y * 10, (int) option.destination.z * 10))
                ).apply(object, (stream) -> {
                            var array = stream.toArray();
                            return new RayOfFrostRayParticleOptions(particleType, new Vec3(array[0], array[1], array[2]));
                        }
                ));
    }

    private final ParticleType<RayOfFrostRayParticleOptions> type;
    private final Vec3 destination;

    public static RayOfFrostRayParticleOptions inner(Vec3 destination) {
        return new RayOfFrostRayParticleOptions(ParticleRegistry.RAY_OF_FROST_INNER_PARTICLE.get(), destination);
    }

    public static RayOfFrostRayParticleOptions outer(Vec3 destination) {
        return new RayOfFrostRayParticleOptions(ParticleRegistry.RAY_OF_FROST_OUTER_PARTICLE.get(), destination);
    }

    public RayOfFrostRayParticleOptions(ParticleType<RayOfFrostRayParticleOptions> type, Vec3 destination) {
        this.type = type;
        this.destination = destination;
    }

    public RayOfFrostRayParticleOptions(ParticleType<RayOfFrostRayParticleOptions> type, double x, double y, double z) {
        this(type, new Vec3(x, y, z));
    }

    @Override
    public ParticleType<RayOfFrostRayParticleOptions> getType() {
        return type;
    }

    public Vec3 getDestination() {
        return this.destination;
    }

}
