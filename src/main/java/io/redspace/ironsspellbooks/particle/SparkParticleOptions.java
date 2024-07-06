package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

public class SparkParticleOptions implements ParticleOptions {
    private final Vector3f color;

    public SparkParticleOptions(Vector3f color) {
        this.color = color;
    }

    public SparkParticleOptions(float r, float g, float b) {
        this(new Vector3f(r, g, b));
    }

    //For networking. Encoder/Decoder functions very intuitive
    public static StreamCodec<? super ByteBuf, SparkParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.color.x);
                buf.writeFloat(option.color.y);
                buf.writeFloat(option.color.z);
            },
            (buf) -> {
                return new SparkParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat());
            }
    );

    //For command only?
    public static MapCodec<SparkParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.FLOAT.fieldOf("r").forGetter(p -> p.color.x),
                    Codec.FLOAT.fieldOf("g").forGetter(p -> p.color.y),
                    Codec.FLOAT.fieldOf("b").forGetter(p -> p.color.z)
            ).apply(object, SparkParticleOptions::new
            ));

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.SPARK_PARTICLE.get();
    }
}
