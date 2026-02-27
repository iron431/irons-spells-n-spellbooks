package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record SwirlingParticleOptions(
        ParticleOptions particleOptions,
        Vec3 normal, Vec3 up,
        Vec3 heightWidthSpeed,
        Vec3 deltaHeightWidthSpeed
) implements ParticleOptions {

    public static StreamCodec<RegistryFriendlyByteBuf, SwirlingParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, SwirlingParticleOptions::particleOptions,
            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::normal,
            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::up,
            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::heightWidthSpeed,
            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::deltaHeightWidthSpeed,
            SwirlingParticleOptions::new
    );

    public static MapCodec<SwirlingParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    ParticleTypes.CODEC.fieldOf("particle").forGetter(SwirlingParticleOptions::particleOptions),
                    Vec3.CODEC.fieldOf("normal").forGetter(SwirlingParticleOptions::normal),
                    Vec3.CODEC.fieldOf("up").forGetter(SwirlingParticleOptions::up),
                    Vec3.CODEC.fieldOf("hws").forGetter(SwirlingParticleOptions::heightWidthSpeed),
                    Vec3.CODEC.fieldOf("dhws").forGetter(SwirlingParticleOptions::deltaHeightWidthSpeed)
            ).apply(builder, SwirlingParticleOptions::new
            ));

    public @NotNull ParticleType<SwirlingParticleOptions> getType() {
        return ParticleRegistry.SWIRLING_PARTICLE.get();
    }
}
