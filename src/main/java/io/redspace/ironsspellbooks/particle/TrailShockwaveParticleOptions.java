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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

public class TrailShockwaveParticleOptions extends DustParticleOptionsBase implements IShockwaveParticleOptions {
    //Shadows dumb private scale
    protected final float scale;
    protected final boolean fullbright;
    protected final String trailParticle;

    public TrailShockwaveParticleOptions(Vector3f color, float scale, boolean glowing, String trailParticle) {
        super(color, scale);
        //Super clamped scale to 4
        this.scale = Math.max(this.getScale(), scale);
        this.fullbright = glowing;
        this.trailParticle = trailParticle;
    }

    public TrailShockwaveParticleOptions(Vector3f color, float scale, boolean glowing, ParticleType<?> trailParticle) {
        this(color, scale, glowing, Objects.requireNonNull(ForgeRegistries.PARTICLE_TYPES.getKey(trailParticle)).toString());
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
    public String trailParticleRaw() {
        return trailParticle;
    }

    @Override
    public Vector3f color() {
        return color;
    }

    @Override
    public Optional<ParticleOptions> trailParticle() {
        try {
            var type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(trailParticle));
            if (type instanceof ParticleOptions particleOptions) {
                return Optional.of(particleOptions);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    /*
        Copied From Dust Particle Options
         */
    public static final Codec<IShockwaveParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) ->
            p_175793_.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((option) -> ((TrailShockwaveParticleOptions) option).color),
                    Codec.FLOAT.fieldOf("scale").forGetter((option) -> ((TrailShockwaveParticleOptions) option).scale),
                    Codec.BOOL.fieldOf("fullbright").forGetter((option) -> ((TrailShockwaveParticleOptions) option).fullbright),
                    Codec.STRING.fieldOf("trailParticle").forGetter((option) -> ((TrailShockwaveParticleOptions) option).trailParticle)
            ).apply(p_175793_, TrailShockwaveParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<IShockwaveParticleOptions> DESERIALIZER = new Deserializer<IShockwaveParticleOptions>() {
        public @NotNull IShockwaveParticleOptions fromCommand(@NotNull ParticleType<IShockwaveParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(p_123690_);
            p_123690_.expect(' ');
            float f = p_123690_.readFloat();
            p_123690_.expect(' ');
            boolean glowing = p_123690_.readBoolean();
            p_123690_.expect(' ');
            String trailParticle = p_123690_.readString();
            return new TrailShockwaveParticleOptions(vector3f, f, glowing, trailParticle);
        }

        public @NotNull IShockwaveParticleOptions fromNetwork(@NotNull ParticleType<IShockwaveParticleOptions> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new TrailShockwaveParticleOptions(DustParticleOptionsBase.readVector3f(p_123693_), p_123693_.readFloat(), p_123693_.readBoolean(), p_123693_.readUtf());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        super.writeToNetwork(pBuffer);
        pBuffer.writeBoolean(fullbright);
        pBuffer.writeUtf(trailParticle);
    }

    public @NotNull ParticleType<IShockwaveParticleOptions> getType() {
        return ParticleRegistry.TRAIL_SHOCKWAVE_PARTICLE.get();
    }
}
