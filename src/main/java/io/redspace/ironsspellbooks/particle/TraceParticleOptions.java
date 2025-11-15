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

public class TraceParticleOptions implements ParticleOptions {
    public final Vector3f color, destination;

    public TraceParticleOptions(Vector3f destination, Vector3f color) {
        this.color = color;
        this.destination = destination;
    }

    public TraceParticleOptions(float x, float y, float z, float r, float g, float b) {
        this(new Vector3f(x, y, z), new Vector3f(r, g, b));
    }

    //For networking. Encoder/Decoder functions very intuitive
    public static StreamCodec<? super ByteBuf, TraceParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.destination.x);
                buf.writeFloat(option.destination.y);
                buf.writeFloat(option.destination.z);
                buf.writeFloat(option.color.x);
                buf.writeFloat(option.color.y);
                buf.writeFloat(option.color.z);
            },
            (buf) -> {
                return new TraceParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(),buf.readFloat(), buf.readFloat(), buf.readFloat());
            }
    );

    //For command only?
    public static MapCodec<TraceParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.FLOAT.fieldOf("x").forGetter(p -> p.destination.x),
                    Codec.FLOAT.fieldOf("y").forGetter(p -> p.destination.y),
                    Codec.FLOAT.fieldOf("z").forGetter(p -> p.destination.z),
                    Codec.FLOAT.fieldOf("r").forGetter(p -> p.color.x),
                    Codec.FLOAT.fieldOf("g").forGetter(p -> p.color.y),
                    Codec.FLOAT.fieldOf("b").forGetter(p -> p.color.z)
            ).apply(object, TraceParticleOptions::new
            ));

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.TRACE_PARTICLE.get();
    }
}
