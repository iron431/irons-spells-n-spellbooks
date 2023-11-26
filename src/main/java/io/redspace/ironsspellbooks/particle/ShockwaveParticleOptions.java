package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ShockwaveParticleOptions extends DustParticleOptionsBase {
    //Shadow's dumb private scale
    private final float scale;

    public ShockwaveParticleOptions(Vector3f color, float scale) {
        super(color, scale);
        //Super clamped scale to 4
        this.scale = Math.max(this.getScale(), scale);
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    /*
        Copied From Dust Particle Options
         */
    public static final Codec<ShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) -> p_175793_.group(Vector3f.CODEC.fieldOf("color").forGetter((p_175797_) -> p_175797_.color), Codec.FLOAT.fieldOf("scale").forGetter((p_175795_) -> p_175795_.scale)).apply(p_175793_, ShockwaveParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<ShockwaveParticleOptions> DESERIALIZER = new Deserializer<ShockwaveParticleOptions>() {
        public @NotNull ShockwaveParticleOptions fromCommand(@NotNull ParticleType<ShockwaveParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(p_123690_);
            p_123690_.expect(' ');
            float f = p_123690_.readFloat();
            return new ShockwaveParticleOptions(vector3f, f);
        }

        public @NotNull ShockwaveParticleOptions fromNetwork(@NotNull ParticleType<ShockwaveParticleOptions> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new ShockwaveParticleOptions(DustParticleOptionsBase.readVector3f(p_123693_), p_123693_.readFloat());
        }
    };

    public @NotNull ParticleType<ShockwaveParticleOptions> getType() {
        return ParticleRegistry.SHOCKWAVE_PARTICLE.get();
    }
}
