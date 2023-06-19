package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class ZapParticleOption implements ParticleOptions {
    public static final Codec<ZapParticleOption> CODEC = RecordCodecBuilder.create((p_235978_) -> {
        return p_235978_.group(PositionSource.CODEC.fieldOf("destination").forGetter((p_235982_) -> {
            return p_235982_.destination;
        })).apply(p_235978_, ZapParticleOption::new);
    });
    public static final ParticleOptions.Deserializer<ZapParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ZapParticleOption>() {
        public ZapParticleOption fromCommand(ParticleType<ZapParticleOption> p_175859_, StringReader p_175860_) throws CommandSyntaxException {
            p_175860_.expect(' ');
            float f = (float)p_175860_.readDouble();
            p_175860_.expect(' ');
            float f1 = (float)p_175860_.readDouble();
            p_175860_.expect(' ');
            float f2 = (float)p_175860_.readDouble();
            BlockPos blockpos = new BlockPos((double)f, (double)f1, (double)f2);
            return new ZapParticleOption(new BlockPositionSource(blockpos));
        }

        public ZapParticleOption fromNetwork(ParticleType<ZapParticleOption> p_175862_, FriendlyByteBuf p_175863_) {
            PositionSource positionsource = PositionSourceType.fromNetwork(p_175863_);
            return new ZapParticleOption(positionsource);
        }
    };
    private final PositionSource destination;

    public ZapParticleOption(PositionSource p_235975_) {
        this.destination = p_235975_;
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        PositionSourceType.toNetwork(this.destination, pBuffer);
    }

    public String writeToString() {
        Vec3 vec3 = this.destination.getPosition((Level)null).get();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2);
    }

    public ParticleType<ZapParticleOption> getType() {
        return ParticleRegistry.ZAP_PARTICLE.get();
    }

    public PositionSource getDestination() {
        return this.destination;
    }

}
