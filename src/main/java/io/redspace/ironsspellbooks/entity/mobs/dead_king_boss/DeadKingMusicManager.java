package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;

import java.util.HashSet;
import java.util.Set;

public class DeadKingMusicManager {
    static final SoundSource SOUND_SOURCE = SoundSource.RECORDS;
    static final int FIRST_PHASE_MELODY_LENGTH_MILIS = 28790;
    static final int SECOND_PHASE_MELODY_LENGTH_MILIS = 26490;
    final DeadKingBoss boss;
    final SoundManager soundManager;
    FadeableSoundInstance beginSound;
    FadeableSoundInstance firstPhaseMelody;
    FadeableSoundInstance firstPhaseAccent;
    FadeableSoundInstance firstPhaseDrums;

    FadeableSoundInstance secondPhaseMelody;
    FadeableSoundInstance secondPhaseAccent;
    FadeableSoundInstance secondPhaseDrums;
    Set<FadeableSoundInstance> layers = new HashSet<>();
    private int accentStage = 0;
    private long lastMilisPlayed;
    private boolean hasPlayedIntro;
    private boolean hasPlayedSuspense;
    private boolean hasPlayedFinale;

    /**
     * 0 = intro music
     * 1 = first boss phase looping
     */
    DeadKingBoss.Phases stage;

    public DeadKingMusicManager(DeadKingBoss boss) {
        this.boss = boss;
        this.soundManager = Minecraft.getInstance().getSoundManager();
        stage = DeadKingBoss.Phases.values()[boss.getPhase()];
        beginSound = new FadeableSoundInstance(SoundRegistry.DEAD_KING_MUSIC_INTRO.get(), SOUND_SOURCE, false);
        firstPhaseMelody = new FadeableSoundInstance(SoundRegistry.DEAD_KING_FIRST_PHASE_MELODY.get(), SOUND_SOURCE, true);
        firstPhaseAccent = new FadeableSoundInstance(SoundRegistry.DEAD_KING_FIRST_PHASE_ACCENT_01.get(), SOUND_SOURCE, false);
        firstPhaseDrums = new FadeableSoundInstance(SoundRegistry.DEAD_KING_DRUM_LOOP.get(), SOUND_SOURCE, false);

        secondPhaseMelody = new FadeableSoundInstance(SoundRegistry.DEAD_KING_SECOND_PHASE_MELODY.get(), SOUND_SOURCE, true);
        secondPhaseAccent = new FadeableSoundInstance(SoundRegistry.DEAD_KING_SECOND_PHASE_ACCENT_01.get(), SOUND_SOURCE, false);
        secondPhaseDrums = new FadeableSoundInstance(SoundRegistry.DEAD_KING_SECOND_PHASE_DRUMS.get(), SOUND_SOURCE, false);
        init();
    }

    private void init() {
        soundManager.stop(null, SoundSource.MUSIC);
        switch (stage) {

            case FirstPhase -> addLayer(beginSound);
            case FinalPhase -> initSecondPhase();
        }
    }

    public void tick() {
        if (boss.isDeadOrDying()) {
            //TODO: play different death music on phase 1 death??
            triggerStop();
            if (!hasPlayedFinale) {
                soundManager.play(new FadeableSoundInstance(SoundRegistry.DEAD_KING_FINALE.get(), SOUND_SOURCE, false));
                hasPlayedFinale = true;
            }
            return;
        }
        if (stage == DeadKingBoss.Phases.FirstPhase) {
            if (!hasPlayedIntro) {
                if (!soundManager.isActive(beginSound)) {
                    hasPlayedIntro = true;
                    initFirstPhase();
                }
            } else if (lastMilisPlayed + FIRST_PHASE_MELODY_LENGTH_MILIS < System.currentTimeMillis()) {
                //alternate accents, trigger silence if the phase ends
                if (accentStage % 2 == 1) {
                    playAccent(firstPhaseDrums);
                } else {
                    playAccent(firstPhaseAccent);
                }
            }
            if (boss.isPhase(DeadKingBoss.Phases.Transitioning)) {
                stage = DeadKingBoss.Phases.Transitioning;
                //todo: something other than a fade?
                triggerStop();
            }
        } else if (stage == DeadKingBoss.Phases.Transitioning) {
            if (!hasPlayedSuspense) {
                addLayer(new FadeableSoundInstance(SoundRegistry.DEAD_KING_SUSPENSE.get(), SOUND_SOURCE, false));
                hasPlayedSuspense = true;
            }
            if (boss.isPhase(DeadKingBoss.Phases.FinalPhase)) {
                stage = DeadKingBoss.Phases.FinalPhase;
                initSecondPhase();
            }
        } else if (stage == DeadKingBoss.Phases.FinalPhase) {
//            if (lastMilisPlayed + FIRST_PHASE_MELODY_LENGTH_MILIS < System.currentTimeMillis()) {
//                //alternate accents, trigger silence if the phase ends
//                if (accentStage % 2 == 1) {
//                    playAccent(secondPhaseAccent);
//                } else {
//                    playAccent(secondPhaseDrums);
//                }
//            }
        }
    }

    private void addLayer(FadeableSoundInstance soundInstance) {
        soundManager.play(soundInstance);
        layers.add(soundInstance);
    }

    private void playAccent(FadeableSoundInstance soundInstance) {
        accentStage++;
        lastMilisPlayed = System.currentTimeMillis();
        addLayer(soundInstance);
    }

    public void triggerStop() {
        layers.forEach(FadeableSoundInstance::triggerStop);
    }

    private void initFirstPhase() {
        accentStage = 0;
        addLayer(firstPhaseMelody);
        addLayer(firstPhaseDrums);
        playAccent(firstPhaseAccent);
    }

    private void initSecondPhase() {
        accentStage = 0;
//        addLayer(secondPhaseMelody);
//        playAccent(secondPhaseDrums);
        addLayer(new FadeableSoundInstance(SoundRegistry.DEAD_KING_SECOND_PHASE_MELODY_ALT.get(), SOUND_SOURCE, true));
    }
}
