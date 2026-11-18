package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class CauldronParticleOptions implements ParticleOptions {
    private static final float SPRITE_BRIGHTENING = 2.5f;

    private final ParticleType<CauldronParticleOptions> type;
    private final BlockPos cauldronPos;

    public CauldronParticleOptions(ParticleType<CauldronParticleOptions> type, BlockPos cauldronPos) {
        this.type = type;
        this.cauldronPos = cauldronPos;
    }

    public static Codec<CauldronParticleOptions> codec(ParticleType<CauldronParticleOptions> type) {
        return BlockPos.CODEC.xmap(pos -> new CauldronParticleOptions(type, pos), CauldronParticleOptions::cauldronPos).fieldOf("pos").codec();
    }

    public static final ParticleOptions.Deserializer<CauldronParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull CauldronParticleOptions fromCommand(@NotNull ParticleType<CauldronParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int x = reader.readInt();
            reader.expect(' ');
            int y = reader.readInt();
            reader.expect(' ');
            int z = reader.readInt();
            return new CauldronParticleOptions(type, new BlockPos(x, y, z));
        }

        @Override
        public @NotNull CauldronParticleOptions fromNetwork(@NotNull ParticleType<CauldronParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new CauldronParticleOptions(type, buf.readBlockPos());
        }
    };

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeBlockPos(cauldronPos);
    }

    @Override
    public @NotNull String writeToString() {
        return cauldronPos.getX() + " " + cauldronPos.getY() + " " + cauldronPos.getZ();
    }
}
