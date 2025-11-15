package io.redspace.ironsspellbooks.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class EnderSlashParticleOptions implements ParticleOptions {
    public final float scale;
    public final float xf;
    public final float yf;
    public final float zf;
    public final float xu;
    public final float yu;
    public final float zu;

    public EnderSlashParticleOptions(float xf, float yf, float zf, float xu, float yu, float zu, float scale) {
        this.scale = scale;
        this.xf = xf;
        this.yf = yf;
        this.zf = zf;
        this.xu = xu;
        this.yu = yu;
        this.zu = zu;
    }

    public static StreamCodec<? super ByteBuf, EnderSlashParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.xf);
                buf.writeFloat(option.yf);
                buf.writeFloat(option.zf);
                buf.writeFloat(option.xu);
                buf.writeFloat(option.yu);
                buf.writeFloat(option.zu);
                buf.writeFloat(option.scale);
            },
            (buf) -> new EnderSlashParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
    );

    public static MapCodec<EnderSlashParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.FLOAT.fieldOf("xf").forGetter(p -> ((EnderSlashParticleOptions) p).xf),
                    Codec.FLOAT.fieldOf("yf").forGetter(p -> ((EnderSlashParticleOptions) p).yf),
                    Codec.FLOAT.fieldOf("zf").forGetter(p -> ((EnderSlashParticleOptions) p).zf),
                    Codec.FLOAT.fieldOf("xu").forGetter(p -> ((EnderSlashParticleOptions) p).xu),
                    Codec.FLOAT.fieldOf("yu").forGetter(p -> ((EnderSlashParticleOptions) p).yu),
                    Codec.FLOAT.fieldOf("zu").forGetter(p -> ((EnderSlashParticleOptions) p).zu),
                    Codec.FLOAT.fieldOf("scale").forGetter(p -> ((EnderSlashParticleOptions) p).scale)
            ).apply(object, EnderSlashParticleOptions::new
            ));

    public @NotNull ParticleType<EnderSlashParticleOptions> getType() {
        return ParticleRegistry.ENDER_SLASH_PARTICLE.get();
    }
}
