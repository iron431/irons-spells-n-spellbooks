package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record SwirlingParticleOptions(
        ParticleOptions particleOptions,
        Vec3 normal, Vec3 up,
        Vec3 heightWidthSpeed,
        Vec3 deltaHeightWidthSpeed
) implements ParticleOptions {

//    public static StreamCodec<RegistryFriendlyByteBuf, SwirlingParticleOptions> STREAM_CODEC = StreamCodec.composite(
//            ParticleTypes.STREAM_CODEC, SwirlingParticleOptions::particleOptions,
//            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::normal,
//            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::up,
//            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::heightWidthSpeed,
//            ByteBufCodecs.fromCodec(Vec3.CODEC), SwirlingParticleOptions::deltaHeightWidthSpeed,
//            SwirlingParticleOptions::new
//    );

    public static Codec<SwirlingParticleOptions> MAP_CODEC = RecordCodecBuilder.create(builder ->
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

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, this.particleOptions.getType());
        particleOptions.writeToNetwork(buf);
        writeVec3(normal, buf);
        writeVec3(up, buf);
        writeVec3(heightWidthSpeed, buf);
        writeVec3(deltaHeightWidthSpeed, buf);
    }

    @Override
    public @NotNull String writeToString() {
        return "";
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vec3(x, y, z);
    }

    public static void writeVec3(Vec3 vec3, FriendlyByteBuf buf) {
        buf.writeDouble(vec3.x);
        buf.writeDouble(vec3.y);
        buf.writeDouble(vec3.z);
    }

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<SwirlingParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SwirlingParticleOptions>() {
        public SwirlingParticleOptions fromCommand(ParticleType<SwirlingParticleOptions> p_123645_, StringReader p_123646_) throws CommandSyntaxException {
//            p_123646_.expect(' ');
//            var state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), p_123646_, false).blockState();
//            var vector = DustParticleOptionsBase.readVector3f(p_123646_);
//            return new SwirlingParticleOptions(p_123645_, state, new Vec3(vector.x(), vector.y(), vector.z()));
             throw new SimpleCommandExceptionType(Component.literal("not supported")).create();
        }

        public @NotNull SwirlingParticleOptions fromNetwork(@NotNull ParticleType<SwirlingParticleOptions> p_123692_, @NotNull FriendlyByteBuf buf) {
            ParticleType particletype = buf.readById(BuiltInRegistries.PARTICLE_TYPE);
            var particle = particletype.getDeserializer().fromNetwork(particletype, buf);
            return new SwirlingParticleOptions(particle, readVec3(buf), readVec3(buf), readVec3(buf), readVec3(buf));
        }
    };
}
