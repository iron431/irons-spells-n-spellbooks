package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ShockwaveParticleOptions implements ParticleOptions {
    protected final boolean fullbright;
    private final float scale;
    private final Vector3f color;

    public ShockwaveParticleOptions(Vector3f color, float scale, boolean glowing) {
        this.scale = scale;
        this.color = color;
        this.fullbright = glowing;
    }

    public float getScale() {
        return this.scale;
    }

    public boolean isFullbright() {
        return this.fullbright;
    }


    public Vector3f color() {
        return color;
    }

    public static final MapCodec<ShockwaveParticleOptions> CODEC = RecordCodecBuilder.mapCodec((p_175793_) ->
            p_175793_.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((option) -> option.color),
                    Codec.FLOAT.fieldOf("scale").forGetter((option) -> option.scale),
                    Codec.BOOL.fieldOf("fullbright").forGetter((option) -> option.fullbright)
            ).apply(p_175793_, ShockwaveParticleOptions::new));

    public static StreamCodec<? super ByteBuf, ShockwaveParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.color.x);
                buf.writeFloat(option.color.y);
                buf.writeFloat(option.color.z);
                buf.writeFloat(option.scale);
                buf.writeBoolean(option.fullbright);
            },
            (buf) -> {
                return new ShockwaveParticleOptions(new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()), buf.readFloat(), buf.readBoolean());
            }
    );

    public @NotNull ParticleType<ShockwaveParticleOptions> getType() {
        return ParticleRegistry.SHOCKWAVE_PARTICLE.get();
    }
}
