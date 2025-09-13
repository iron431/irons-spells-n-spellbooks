package io.redspace.ironsspellbooks.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.stream.IntStream;

public class ZapParticleOption implements ParticleOptions {
    public static final Codec<ZapParticleOption> CODEC = Codec.INT_STREAM.comapFlatMap((p_121967_) -> {
        return Util.fixedSize(p_121967_, 3).map((vec3) -> {
            return new ZapParticleOption(new Vec3(vec3[0] / 10f, vec3[1] / 10f, vec3[2] / 10f));
        });
    }, (p_121924_) -> {
        return IntStream.of((int) (p_121924_.destination.x * 10f), (int) (p_121924_.destination.y * 10), (int) (p_121924_.destination.z * 10f));
    });
    //    public static final Codec<ZapParticleOption> CODEC = RecordCodecBuilder.create((p_235978_) -> {
//        return p_235978_.group(PositionSource.CODEC.fieldOf("destination").forGetter((p_235982_) -> {
//            return p_235982_.destination;
//        })).apply(p_235978_, ZapParticleOption::new);
//    });
    public static final ParticleOptions.Deserializer<ZapParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ZapParticleOption>() {
        public ZapParticleOption fromCommand(ParticleType<ZapParticleOption> p_175859_, StringReader p_175860_) throws CommandSyntaxException {
            p_175860_.expect(' ');
            float f = (float) p_175860_.readDouble();
            p_175860_.expect(' ');
            float f1 = (float) p_175860_.readDouble();
            p_175860_.expect(' ');
            float f2 = (float) p_175860_.readDouble();
            return new ZapParticleOption(new Vec3(f, f1, f2));
        }

        public ZapParticleOption fromNetwork(ParticleType<ZapParticleOption> p_175862_, FriendlyByteBuf p_175863_) {
            var positionsource = ZapParticleOption.fromNetwork(p_175863_);
            return new ZapParticleOption(positionsource);
        }
    };
    private final Vec3 destination;

    public ZapParticleOption(Vec3 destination) {
        this.destination = destination;
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        toNetwork(this.destination, pBuffer);
    }

    public String writeToString() {
        Vec3 vec3 = this.destination;
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2);
    }

    public ParticleType<ZapParticleOption> getType() {
        return ParticleRegistry.ZAP_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

    private static Vec3 fromNetwork(FriendlyByteBuf buf) {
        return new Vec3(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
    }

    private static void toNetwork(Vec3 vec, FriendlyByteBuf buf) {
        buf.writeInt((int) (vec.x * 10));
        buf.writeInt((int) (vec.y * 10));
        buf.writeInt((int) (vec.z * 10));
    }
}
