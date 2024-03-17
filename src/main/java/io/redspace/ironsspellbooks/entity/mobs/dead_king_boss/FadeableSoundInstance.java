package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class FadeableSoundInstance extends AbstractTickableSoundInstance {
    boolean starting = false;
    private int transitionTicks;
    private boolean triggerEnd = false;
    private static final int START_TRANSITION_TIME = 40;
    private static final int END_TRANSITION_TIME = 40;

    protected FadeableSoundInstance(SoundEvent soundEvent, SoundSource source, boolean loop) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = loop;
        this.delay = 0;
        this.volume = 1;
        this.starting = false;
    }

    @Override
    public void tick() {
        if (transitionTicks > 0) {
            transitionTicks--;
        }
        if (starting) {
            this.volume = 1f - ((float) transitionTicks / START_TRANSITION_TIME);
            if (transitionTicks == 0) {
                starting = false;
            }
        }
        if (triggerEnd) {
            this.volume = ((float) transitionTicks / END_TRANSITION_TIME);
            if (transitionTicks == 0) {
                this.stop();
            }
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void triggerStop() {
        this.triggerEnd = true;
        if (volume < 1f) {
            transitionTicks = (int) (END_TRANSITION_TIME * volume);
        } else {
            transitionTicks = END_TRANSITION_TIME;
        }
    }

    public void triggerStart() {
        this.triggerEnd = false;
        if (volume < 1f) {
            transitionTicks = (int) (START_TRANSITION_TIME * volume);
        } else {
            transitionTicks = START_TRANSITION_TIME;
        }
        starting = true;
    }
}
