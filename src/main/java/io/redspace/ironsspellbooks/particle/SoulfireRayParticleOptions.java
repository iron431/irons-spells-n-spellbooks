package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoulfireRayParticleOptions implements ParticleOptions {
    public static final MapCodec<SoulfireRayParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(option -> option.destination.x),
                    Codec.DOUBLE.fieldOf("y").forGetter(option -> option.destination.y),
                    Codec.DOUBLE.fieldOf("z").forGetter(option -> option.destination.z)
            ).apply(instance, SoulfireRayParticleOptions::new)
    );

    public static final Codec<SoulfireRayParticleOptions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(option -> option.destination.x),
                    Codec.DOUBLE.fieldOf("y").forGetter(option -> option.destination.y),
                    Codec.DOUBLE.fieldOf("z").forGetter(option -> option.destination.z)
            ).apply(instance, SoulfireRayParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<SoulfireRayParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull SoulfireRayParticleOptions fromCommand(@NotNull ParticleType<SoulfireRayParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            return new SoulfireRayParticleOptions(reader.readDouble(), reader.readDouble(), reader.readDouble());
        }

        @Override
        public @NotNull SoulfireRayParticleOptions fromNetwork(@NotNull ParticleType<SoulfireRayParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new SoulfireRayParticleOptions(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    };

    private final Vec3 destination;

    public SoulfireRayParticleOptions(Vec3 destination) {
        this.destination = destination;
    }

    public SoulfireRayParticleOptions(double x, double y, double z) {
        this(new Vec3(x, y, z));
    }

    @Override
    public @NotNull ParticleType<SoulfireRayParticleOptions> getType() {
        return ParticleRegistry.SOULFIRE_RAY_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(destination.x);
        buf.writeDouble(destination.y);
        buf.writeDouble(destination.z);
    }

    @Override
    public @NotNull String writeToString() {
        return destination.x + " " + destination.y + " " + destination.z;
    }
}
