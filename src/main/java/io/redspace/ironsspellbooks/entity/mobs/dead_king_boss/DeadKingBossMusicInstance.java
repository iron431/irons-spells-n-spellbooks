package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public class DeadKingBossMusicInstance extends AbstractTickableSoundInstance {
    final DeadKingBoss boss;
    boolean starting = true;
    boolean ending = false;
    boolean triggerEnd = false;
    int transitionTicks = START_TRANSITION_TIME;
    private static final int START_TRANSITION_TIME = 40;
    private static final int END_TRANSITION_TIME = 40;

    protected DeadKingBossMusicInstance(DeadKingBoss boss) {
        super(SoundRegistry.DEAD_KING_DRUM_LOOP.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = 0;
        this.boss = boss;
        this.starting = true;
        this.transitionTicks = START_TRANSITION_TIME;
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
        if (triggerEnd || boss.isDeadOrDying()) {
            starting = false;
            if (!ending) {
                ending = true;
                transitionTicks = END_TRANSITION_TIME;
            }
            this.volume = ((float) transitionTicks / END_TRANSITION_TIME);
            if (transitionTicks == 0) {
                this.stop();
            }
        }

        if (boss.isPhase(DeadKingBoss.Phases.FinalPhase)) {
            this.pitch = 1.75f;
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void triggerStop() {
        this.triggerEnd = true;
    }
}
