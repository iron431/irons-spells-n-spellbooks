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

public class ZapParticleOption implements ParticleOptions {
    //truncate to 1 decimal place for packet space because we are so optimized
    public static StreamCodec<? super ByteBuf, ZapParticleOption> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeInt((int) option.destination.x * 10);
                buf.writeInt((int) option.destination.y * 10);
                buf.writeInt((int) option.destination.z * 10);
            },
            (buf) -> {
                return new ZapParticleOption(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
            }
    );

    public static MapCodec<ZapParticleOption> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.INT_STREAM.fieldOf("destination").forGetter((option) -> IntStream.of((int) option.destination.x * 10, (int) option.destination.y * 10, (int) option.destination.z * 10))
            ).apply(object, (stream) -> {
                        var array = stream.toArray();
                        return new ZapParticleOption(new Vec3(array[0] / 10f, array[1] / 10f, array[2] / 10f));
                    }
            ));

    private final Vec3 destination;

    public ZapParticleOption(Vec3 destination) {
        this.destination = destination;
    }

    public ZapParticleOption(float x, float y, float z) {
        this(new Vec3(x, y, z));
    }

    public ParticleType<ZapParticleOption> getType() {
        return ParticleRegistry.ZAP_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

}
