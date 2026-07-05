package io.redspace.skillcasting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record PlayableSound(Holder<SoundEvent> soundEventHolder, float volume, float minPitch, float maxPitch) {

    public static final Codec<PlayableSound> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.SOUND_EVENT.holderByNameCodec().fieldOf("sound").forGetter(PlayableSound::soundEventHolder),
            Codec.FLOAT.fieldOf("volume").forGetter(PlayableSound::volume),
            Codec.FLOAT.fieldOf("min_pitch").forGetter(PlayableSound::minPitch),
            Codec.FLOAT.fieldOf("max_pitch").forGetter(PlayableSound::maxPitch)
    ).apply(builder, PlayableSound::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayableSound> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT), PlayableSound::soundEventHolder,
            ByteBufCodecs.FLOAT, PlayableSound::volume,
            ByteBufCodecs.FLOAT, PlayableSound::minPitch,
            ByteBufCodecs.FLOAT, PlayableSound::maxPitch,
            PlayableSound::new);

    public float samplePitch(RandomSource randomSource) {
        if (maxPitch <= minPitch) {
            return maxPitch;
        }
        return Mth.lerp(randomSource.nextFloat(), minPitch, maxPitch);
    }

    public void play(Level level, double x, double y, double z, SoundSource soundSource) {
        level.playSound(null, x, y, z, this.soundEventHolder.value(), soundSource, this.volume, this.samplePitch(level.getRandom()));
    }

    public void play(Level level, Vec3 vec, SoundSource soundSource) {
        play(level, vec.x, vec.y, vec.z, soundSource);
    }

    /**
     * Helper for turning playable sounds into the option return for skills
     */
    public Optional<PlayableSound> toOpt() {
        return Optional.of(this);
    }

    /* ************
     * Builders
     * ************/

    /**
     * @return "standard" playable sound with volume 2 and pitch [0.9, 1.1]
     */
    public static PlayableSound standard(Holder<SoundEvent> soundEvent) {
        return of(soundEvent, 2f, 0.9f, 1.1f);
    }

    public static PlayableSound of(Holder<SoundEvent> soundEvent, float volume) {
        return of(soundEvent, volume, 1f);
    }

    public static PlayableSound of(Holder<SoundEvent> soundEvent, float volume, float pitch) {
        return new PlayableSound(soundEvent, volume, pitch, pitch);
    }

    public static PlayableSound of(Holder<SoundEvent> soundEvent, float volume, float minPitch, float maxPitch) {
        return new PlayableSound(soundEvent, volume, minPitch, maxPitch);
    }

    /**
     * @return "standard" playable sound with volume 2 and pitch [0.9, 1.1]
     */
    public static PlayableSound standard(SoundEvent soundEvent) {
        return standard(holder(soundEvent));
    }

    public static PlayableSound of(SoundEvent soundEvent, float volume) {
        return of(holder(soundEvent), volume);
    }

    public static PlayableSound of(SoundEvent soundEvent, float volume, float pitch) {
        return of(holder(soundEvent), volume, pitch);
    }

    public static PlayableSound of(SoundEvent soundEvent, float volume, float minPitch, float maxPitch) {
        return of(holder(soundEvent), volume, minPitch, maxPitch);
    }

    private static Holder<SoundEvent> holder(SoundEvent soundEvent) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
    }


}
