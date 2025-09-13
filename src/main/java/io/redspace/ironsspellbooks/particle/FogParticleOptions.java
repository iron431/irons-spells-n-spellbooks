package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class FogParticleOptions extends DustParticleOptionsBase {
    public FogParticleOptions(Vector3f color, float scale) {
        super(color, scale);
    }
    /*
    Copied From Dust Particle Options
     */
    public static final Codec<FogParticleOptions> CODEC = RecordCodecBuilder.create((p_175793_) -> p_175793_.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((p_175797_) -> p_175797_.color), Codec.FLOAT.fieldOf("scale").forGetter((p_175795_) -> p_175795_.scale)).apply(p_175793_, FogParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<FogParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<FogParticleOptions>() {
        public @NotNull FogParticleOptions fromCommand(@NotNull ParticleType<FogParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(p_123690_);
            p_123690_.expect(' ');
            float f = p_123690_.readFloat();
            return new FogParticleOptions(vector3f, f);
        }

        public @NotNull FogParticleOptions fromNetwork(@NotNull ParticleType<FogParticleOptions> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new FogParticleOptions(DustParticleOptionsBase.readVector3f(p_123693_), p_123693_.readFloat());
        }
    };

    public @NotNull ParticleType<FogParticleOptions> getType() {
        return ParticleRegistry.FOG_PARTICLE.get();
    }
}
