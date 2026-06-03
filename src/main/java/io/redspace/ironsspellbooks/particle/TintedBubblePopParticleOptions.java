package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TintedBubblePopParticleOptions implements ParticleOptions {
    public static final MapCodec<TintedBubblePopParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(TintedBubblePopParticleOptions::cauldronPos)
            ).apply(instance, TintedBubblePopParticleOptions::new)
    );

    public static final StreamCodec<? super RegistryFriendlyByteBuf, TintedBubblePopParticleOptions> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TintedBubblePopParticleOptions::cauldronPos,
            TintedBubblePopParticleOptions::new
    );

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
}
