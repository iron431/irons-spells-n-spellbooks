package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

public class ShockwaveParticleOptions extends DustParticleOptionsBase implements IShockwaveParticleOptions {
    //Shadows dumb private scale
    protected final float scale;
    protected final boolean fullbright;

    public ShockwaveParticleOptions(Vector3f color, float scale, boolean glowing) {
        super(color, scale);
        //Super clamped scale to 4
        this.scale = Math.max(this.getScale(), scale);
        this.fullbright = glowing;
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public boolean isFullbright() {
        return this.fullbright;
    }

    @Override
    public Optional<ParticleOptions> trailParticle() {
        return Optional.empty();
    }

    @Override
    public String trailParticleRaw() {
        return "";
    }

    @Override
    public Vector3f color() {
        return color;
    }

    /*
        Copied From Dust Particle Options
         */
    //Correct 1.20.1 codec port:
//    public static final Codec<ShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
//            p_175793_.group(
//                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((p_175797_) -> p_175797_.color),
//                    Codec.FLOAT.fieldOf("scale").forGetter((p_175795_) -> p_175795_.scale),
//                    Codec.BOOL.fieldOf("fullbright").forGetter((p_175795_) -> p_175795_.fullbright)
//            ).apply(p_175793_, ShockwaveParticleOptions::new));
    //1.19.2 new shit
    public static final Codec<IShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
            p_175793_.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> ((ShockwaveParticleOptions) options).color),
                    Codec.FLOAT.fieldOf("scale").forGetter((options) -> ((ShockwaveParticleOptions) options).scale),
                    Codec.BOOL.fieldOf("fullbright").forGetter((options) -> ((ShockwaveParticleOptions) options).fullbright)
            ).apply(p_175793_, ShockwaveParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<IShockwaveParticleOptions> DESERIALIZER = new Deserializer<IShockwaveParticleOptions>() {
        public @NotNull IShockwaveParticleOptions fromCommand(@NotNull ParticleType<IShockwaveParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(p_123690_);
            p_123690_.expect(' ');
            float f = p_123690_.readFloat();
            p_123690_.expect(' ');
            boolean glowing = p_123690_.readBoolean();
            return new ShockwaveParticleOptions(vector3f, f, glowing);
        }

        public @NotNull IShockwaveParticleOptions fromNetwork(@NotNull ParticleType<IShockwaveParticleOptions> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new ShockwaveParticleOptions(DustParticleOptionsBase.readVector3f(p_123693_), p_123693_.readFloat(), p_123693_.readBoolean());
        }
    };

    public @NotNull ParticleType<IShockwaveParticleOptions> getType() {
        return ParticleRegistry.SHOCKWAVE_PARTICLE.get();
    }
}
