package io.redspace.skillcasting.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record PlayableSound(Holder<SoundEvent> soundEventHolder, float volume, float minPitch, float maxPitch) {

    public float samplePitch(RandomSource randomSource) {
        if (maxPitch <= minPitch) {
            return maxPitch;
        }
        return Mth.lerp(randomSource.nextFloat(), minPitch, maxPitch);
    }

    public static PlayableSound of(Holder<SoundEvent> soundEvent) {
        return of(soundEvent, 1f);
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

    public static PlayableSound of(SoundEvent soundEvent) {
        return of(holder(soundEvent));
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
