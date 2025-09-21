package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ShockwaveParticleOptions extends DustParticleOptionsBase {
    protected final float unclampedScale;
    protected final boolean fullbright;

    public ShockwaveParticleOptions(Vector3f color, float scale, boolean glowing, String trailParticle) {
        super(color, scale);
        //Super clamped scale to 4
        this.unclampedScale = scale;
        this.fullbright = glowing;
    }

    public ShockwaveParticleOptions(Vector3f color, float scale, boolean glowing) {
        this(color, scale, glowing, "");
    }

    @Override
    public float getScale() {
        return this.unclampedScale;
    }

    public boolean isFullbright() {
        return this.fullbright;
    }

    public Vector3f color() {
        return color;
    }

    /*
        Copied From Dust Particle Options
         */
    public static final Codec<ShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
            p_175793_.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((option) -> option.color),
                    Codec.FLOAT.fieldOf("scale").forGetter((option) -> option.unclampedScale),
                    Codec.BOOL.fieldOf("fullbright").forGetter((option) -> option.fullbright)
            ).apply(p_175793_, ShockwaveParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<ShockwaveParticleOptions> DESERIALIZER = new Deserializer<ShockwaveParticleOptions>() {
        public @NotNull ShockwaveParticleOptions fromCommand(@NotNull ParticleType<ShockwaveParticleOptions> p_123689_, @NotNull StringReader reader) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(reader);
            reader.expect(' ');
            float f = reader.readFloat();
            reader.expect(' ');
            boolean glowing = reader.readBoolean();
            return new ShockwaveParticleOptions(vector3f, f, glowing);
        }

        public @NotNull ShockwaveParticleOptions fromNetwork(@NotNull ParticleType<ShockwaveParticleOptions> p_123692_, @NotNull FriendlyByteBuf buf) {
            return new ShockwaveParticleOptions(DustParticleOptionsBase.readVector3f(buf), buf.readFloat(), buf.readBoolean());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeFloat(this.color.x());
        pBuffer.writeFloat(this.color.y());
        pBuffer.writeFloat(this.color.z());
        pBuffer.writeFloat(this.unclampedScale);
        pBuffer.writeBoolean(fullbright);
    }

    public @NotNull ParticleType<ShockwaveParticleOptions> getType() {
        return ParticleRegistry.SHOCKWAVE_PARTICLE.get();
    }
}
