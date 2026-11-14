package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import org.joml.Vector3f;

public class CauldronParticleOptions implements ParticleOptions {
    private static final float SPRITE_BRIGHTENING = 2.5f;

    private final ParticleType<CauldronParticleOptions> type;
    private final BlockPos cauldronPos;

    public CauldronParticleOptions(ParticleType<CauldronParticleOptions> type, BlockPos cauldronPos) {
        this.type = type;
        this.cauldronPos = cauldronPos;
    }

    public static MapCodec<CauldronParticleOptions> codec(ParticleType<CauldronParticleOptions> type) {
        return BlockPos.CODEC.xmap(pos -> new CauldronParticleOptions(type, pos), CauldronParticleOptions::cauldronPos).fieldOf("pos");
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, CauldronParticleOptions> streamCodec(ParticleType<CauldronParticleOptions> type) {
        return BlockPos.STREAM_CODEC.map(pos -> new CauldronParticleOptions(type, pos), CauldronParticleOptions::cauldronPos);
    }

    public BlockPos cauldronPos() {
        return cauldronPos;
    }

    public Vector3f tint(BlockGetter level) {
        int color = 0xFFFFFFFF;
        if (level.getBlockEntity(cauldronPos) instanceof AlchemistCauldronTile cauldron) {
            color = cauldron.getAverageWaterColor();
        }
        return new Vector3f(brighten(FastColor.ARGB32.red(color)), brighten(FastColor.ARGB32.green(color)), brighten(FastColor.ARGB32.blue(color)));
    }

    private static float brighten(int channel) {
        return Mth.clamp(channel * SPRITE_BRIGHTENING, 0, 255) / 255f;
    }

    @Override
    public ParticleType<CauldronParticleOptions> getType() {
        return type;
    }
}
