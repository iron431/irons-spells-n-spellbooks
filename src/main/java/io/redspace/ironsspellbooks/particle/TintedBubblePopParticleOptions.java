package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class TintedBubblePopParticleOptions implements ParticleOptions {
    public static final MapCodec<TintedBubblePopParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(TintedBubblePopParticleOptions::cauldronPos)
            ).apply(instance, TintedBubblePopParticleOptions::new)
    );

    public static final Codec<TintedBubblePopParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(TintedBubblePopParticleOptions::cauldronPos)
            ).apply(instance, TintedBubblePopParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<TintedBubblePopParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull TintedBubblePopParticleOptions fromCommand(@NotNull ParticleType<TintedBubblePopParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int x = reader.readInt();
            reader.expect(' ');
            int y = reader.readInt();
            reader.expect(' ');
            int z = reader.readInt();
            return new TintedBubblePopParticleOptions(new BlockPos(x, y, z));
        }

        @Override
        public @NotNull TintedBubblePopParticleOptions fromNetwork(@NotNull ParticleType<TintedBubblePopParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new TintedBubblePopParticleOptions(buf.readBlockPos());
        }
    };

    private final BlockPos cauldronPos;

    public TintedBubblePopParticleOptions(BlockPos cauldronPos) {
        this.cauldronPos = cauldronPos;
    }

    public BlockPos cauldronPos() {
        return cauldronPos;
    }

    @Override
    public ParticleType<TintedBubblePopParticleOptions> getType() {
        return ParticleRegistry.TINTED_BUBBLE_POP_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeBlockPos(cauldronPos);
    }

    @Override
    public @NotNull String writeToString() {
        return cauldronPos.getX() + " " + cauldronPos.getY() + " " + cauldronPos.getZ();
    }
}
