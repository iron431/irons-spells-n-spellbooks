package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FallingBlockParticleOption implements ParticleOptions {
    private static final Codec<BlockState> BLOCK_STATE_CODEC = CodecHelper.withAlternative(
            BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec().xmap(Block::defaultBlockState, BlockState::getBlock)
    );
    //    private static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
//    private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);
    private final ParticleType<FallingBlockParticleOption> type;
    private final BlockState state;

    public Vec3 getMotion() {
        return motion;
    }

    private final Vec3 motion;

    public static Codec<FallingBlockParticleOption> codec(ParticleType<FallingBlockParticleOption> particleType) {
        return RecordCodecBuilder.create(builder ->
                builder.group(
                        BLOCK_STATE_CODEC.fieldOf("block_state").forGetter(FallingBlockParticleOption::getState),
                        Vec3.CODEC.optionalFieldOf("motion", Vec3.ZERO).forGetter(FallingBlockParticleOption::getMotion)
                ).apply(builder, (state, motion) -> new FallingBlockParticleOption(particleType, state, motion)));
    }

//    public static StreamCodec<? super RegistryFriendlyByteBuf, FallingBlockParticleOption> streamCodec(ParticleType<FallingBlockParticleOption> particleType) {
//        return StreamCodec.composite(
//                BLOCK_STATE_STREAM_CODEC, FallingBlockParticleOption::getState,
//                ByteBufCodecs.optional(VEC3_STREAM_CODEC).map(opt -> opt.orElse(Vec3.ZERO), vec3 -> vec3 == Vec3.ZERO ? Optional.empty() : Optional.of(vec3)), FallingBlockParticleOption::getMotion,
//                (state, motion) -> new FallingBlockParticleOption(particleType, state, motion)
//        );
//    }

    public FallingBlockParticleOption(ParticleType<FallingBlockParticleOption> type, BlockState state, Vec3 motion) {
        this.type = type;
        this.state = state;
        this.motion = motion;
    }

    public FallingBlockParticleOption(ParticleType<FallingBlockParticleOption> type, BlockState state) {
        this(type, state, Vec3.ZERO);
    }

    public FallingBlockParticleOption(BlockState state, Vec3 motion) {
        this(null/*ParticleRegistry.FALLING_BLOCK_PARTICLE.get()*/, state, motion);
    }

    public FallingBlockParticleOption(BlockState state) {
        this(state, Vec3.ZERO);
    }

    @Override
    public ParticleType<FallingBlockParticleOption> getType() {
        return this.type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeId(Block.BLOCK_STATE_REGISTRY, this.state);

        Vec3 motion = this.getMotion();
        if (motion == Vec3.ZERO) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeDouble(motion.x);
            buf.writeDouble(motion.y);
            buf.writeDouble(motion.z);
        }
    }

    @Override
    public String writeToString() {
        return "";
    }

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<FallingBlockParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<FallingBlockParticleOption>() {
        public @NotNull FallingBlockParticleOption fromCommand(@NotNull ParticleType<FallingBlockParticleOption> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            throw new SimpleCommandExceptionType(() -> "Command support not implemented").create();
        }

        public @NotNull FallingBlockParticleOption fromNetwork(@NotNull ParticleType<FallingBlockParticleOption> p_123692_, @NotNull FriendlyByteBuf buf) {
            return new FallingBlockParticleOption(buf.readById(Block.BLOCK_STATE_REGISTRY), buf.readBoolean() ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()) : Vec3.ZERO);
        }
    };

    public BlockState getState() {
        return this.state;
    }

}
