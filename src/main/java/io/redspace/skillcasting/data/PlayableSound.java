package io.redspace.skillcasting.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record PlayableSound(Holder<SoundEvent> soundEventHolder, float volume, float minPitch, float maxPitch) {

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
