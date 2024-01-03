package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

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
    public static final Codec<IShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
            p_175793_.group(Vector3f.CODEC.fieldOf("color").forGetter(IShockwaveParticleOptions::color),
                    Codec.FLOAT.fieldOf("scale").forGetter(IShockwaveParticleOptions::getScale),
                    Codec.BOOL.fieldOf("fullbright").forGetter(IShockwaveParticleOptions::isFullbright)
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

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        super.writeToNetwork(pBuffer);
        pBuffer.writeBoolean(fullbright);
    }

    public @NotNull ParticleType<IShockwaveParticleOptions> getType() {
        return ParticleRegistry.SHOCKWAVE_PARTICLE.get();
    }
}
