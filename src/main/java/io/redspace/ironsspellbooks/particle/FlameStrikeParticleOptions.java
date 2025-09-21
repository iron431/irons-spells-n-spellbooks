package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class FlameStrikeParticleOptions implements ParticleOptions {
    public final float scale;
    public final float xf;
    public final float yf;
    public final float zf;
    public final boolean mirror, vertical;

    public FlameStrikeParticleOptions(float xf, float yf, float zf, boolean mirror, boolean vertical, float scale) {
        this.scale = scale;
        this.xf = xf;
        this.yf = yf;
        this.zf = zf;
        this.mirror = mirror;
        this.vertical = vertical;
    }

//    public static StreamCodec<? super ByteBuf, FlameStrikeParticleOptions> STREAM_CODEC = StreamCodec.of(
//            (buf, option) -> {
//                buf.writeFloat(option.xf);
//                buf.writeFloat(option.yf);
//                buf.writeFloat(option.zf);
//                buf.writeBoolean(option.mirror);
//                buf.writeBoolean(option.vertical);
//                buf.writeFloat(option.scale);
//            },
//            (buf) -> new FlameStrikeParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readFloat())
//    );

    public static Codec<FlameStrikeParticleOptions> CODEC = RecordCodecBuilder.create(object ->
            object.group(
                    Codec.FLOAT.fieldOf("xf").forGetter(p -> ((FlameStrikeParticleOptions) p).xf),
                    Codec.FLOAT.fieldOf("yf").forGetter(p -> ((FlameStrikeParticleOptions) p).yf),
                    Codec.FLOAT.fieldOf("zf").forGetter(p -> ((FlameStrikeParticleOptions) p).zf),
                    Codec.BOOL.fieldOf("mirror").forGetter(p -> ((FlameStrikeParticleOptions) p).mirror),
                    Codec.BOOL.fieldOf("vertical").forGetter(p -> ((FlameStrikeParticleOptions) p).vertical),
                    Codec.FLOAT.fieldOf("scale").forGetter(p -> ((FlameStrikeParticleOptions) p).scale)
            ).apply(object, FlameStrikeParticleOptions::new
            ));

    public @NotNull ParticleType<FlameStrikeParticleOptions> getType() {
        return ParticleRegistry.FLAME_STRIKE_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        var option = this;
        buf.writeFloat(option.xf);
        buf.writeFloat(option.yf);
        buf.writeFloat(option.zf);
        buf.writeBoolean(option.mirror);
        buf.writeBoolean(option.vertical);
        buf.writeFloat(option.scale);
    }

    @Override
    public String writeToString() {
        return "";
    }

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<FlameStrikeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<FlameStrikeParticleOptions>() {
        public @NotNull FlameStrikeParticleOptions fromCommand(@NotNull ParticleType<FlameStrikeParticleOptions> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            return new FlameStrikeParticleOptions(p_123690_.readFloat(), p_123690_.readFloat(), p_123690_.readFloat(), p_123690_.readBoolean(), p_123690_.readBoolean(), p_123690_.readFloat());
        }

        public @NotNull FlameStrikeParticleOptions fromNetwork(@NotNull ParticleType<FlameStrikeParticleOptions> p_123692_, @NotNull FriendlyByteBuf buf) {
            return new FlameStrikeParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readFloat());
        }
    };
}
