package io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframes;

import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.EventKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.IAbilityHandler;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import java.util.function.Supplier;

public class SoundEventKeyframe<T extends Mob & IAbilityHandler<T>> extends EventKeyframe<T> {
    final Supplier<SoundEvent> soundEvent;
    final float volume, pitchMin, pitchMax;

    public SoundEventKeyframe(int timestamp, Supplier<SoundEvent> soundEvent, float volume, float pitchMin, float pitchMax) {
        super(timestamp);
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
    }

    public SoundEventKeyframe(int timestamp, Supplier<SoundEvent> event, float volume, float pitch) {
        this(timestamp, event, volume, pitch, pitch);
    }

    public SoundEventKeyframe(int timestamp, Supplier<SoundEvent> event, float volume) {
        this(timestamp, event, volume, 1f);
    }

    public SoundEventKeyframe(int timestamp, Supplier<SoundEvent> event) {
        this(timestamp, event, 1f);
    }

    @Override
    public void onEvent(T mob) {
        mob.playSound(soundEvent.get(), volume, Mth.lerp(mob.getRandom().nextFloat(), pitchMin, pitchMax));
    }
}
