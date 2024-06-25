package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BlastwaveParticleOptions implements ParticleOptions {
    private final float scale;
    private final Vector3f color;

    public BlastwaveParticleOptions(Vector3f color, float scale) {
        this.scale = scale;
        this.color = color;
    }
    public float getScale() {
        return this.scale;
    }

    public Vector3f getColor() {
        return this.color;
    }


    public Vector3f color() {
        return color;
    }
    public BlastwaveParticleOptions(float r, float g, float b, float scale) {
        this(new Vector3f(r, g, b), scale);
    }

    private BlastwaveParticleOptions(Vector4f vector4f) {
        this(vector4f.x, vector4f.y, vector4f.z, vector4f.w);
    }

    //For networking. Encoder/Decoder functions very intuitive
    public static StreamCodec<? super ByteBuf, BlastwaveParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.color.x);
                buf.writeFloat(option.color.y);
                buf.writeFloat(option.color.z);
                buf.writeFloat(option.scale);
            },
            (buf) -> {
                return new BlastwaveParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
            }
    );

    //For command only?
    public static MapCodec<BlastwaveParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.FLOAT.fieldOf("r").forGetter(p -> ((BlastwaveParticleOptions) p).color.x),
                    Codec.FLOAT.fieldOf("g").forGetter(p -> ((BlastwaveParticleOptions) p).color.y),
                    Codec.FLOAT.fieldOf("b").forGetter(p -> ((BlastwaveParticleOptions) p).color.z),
                    Codec.FLOAT.fieldOf("scale").forGetter(p -> ((BlastwaveParticleOptions) p).scale)
            ).apply(object, BlastwaveParticleOptions::new
            ));

    public @NotNull ParticleType<BlastwaveParticleOptions> getType() {
        return ParticleRegistry.BLASTWAVE_PARTICLE.get();
    }
}
