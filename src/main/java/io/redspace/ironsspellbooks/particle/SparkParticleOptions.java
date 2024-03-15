package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Locale;

public class SparkParticleOptions implements ParticleOptions {
    protected final Vector3f color;

    public SparkParticleOptions(Vector3f pColor) {
        this.color = pColor;
    }

    public static Vector3f readVector3f(StringReader pStringInput) throws CommandSyntaxException {
        pStringInput.expect(' ');
        float f = pStringInput.readFloat();
        pStringInput.expect(' ');
        float f1 = pStringInput.readFloat();
        pStringInput.expect(' ');
        float f2 = pStringInput.readFloat();
        return new Vector3f(f, f1, f2);
    }

    public static Vector3f readVector3f(FriendlyByteBuf pBuffer) {
        return new Vector3f(pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat());
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.SPARK_PARTICLE.get();
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeFloat(this.color.x());
        pBuffer.writeFloat(this.color.y());
        pBuffer.writeFloat(this.color.z());
    }

    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f", this.color.x(), this.color.y(), this.color.z());
    }

    public Vector3f getColor() {
        return this.color;
    }

    public static final Codec<SparkParticleOptions> CODEC = RecordCodecBuilder.create(
            (p_175793_) -> p_175793_.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((p_175797_) -> p_175797_.color)
            ).apply(p_175793_, SparkParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<SparkParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SparkParticleOptions>() {
        public @NotNull SparkParticleOptions fromCommand(@NotNull ParticleType<SparkParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            Vector3f vector3f = readVector3f(p_123690_);
            return new SparkParticleOptions(vector3f);
        }

        public @NotNull SparkParticleOptions fromNetwork(@NotNull ParticleType<SparkParticleOptions> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new SparkParticleOptions(readVector3f(p_123693_));
        }
    };

}
