package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

import java.util.HashSet;
import java.util.Set;

public class DeadKingMusicManager {
    static final SoundSource SOUND_SOURCE = SoundSource.RECORDS;
    static final int FIRST_PHASE_MELODY_LENGTH_MILIS = 28790;
    final DeadKingBoss boss;
    final SoundManager soundManager;
    FadeOutLoopingSoundInstance beginSound;
    FadeOutLoopingSoundInstance firstPhaseMelody;
    FadeOutLoopingSoundInstance firstPhaseAccent;
    FadeOutLoopingSoundInstance firstPhaseDrums;
    Set<FadeOutLoopingSoundInstance> layers = new HashSet<>();
    private int accentStage = 0;
    private long lastMilisPlayed;

    /**
     * 0 = intro music
     * 1 = first boss phase looping
     */
    int stage;

    public DeadKingMusicManager(DeadKingBoss boss) {
        this.boss = boss;
        this.soundManager = Minecraft.getInstance().getSoundManager();
        stage = 0;
        beginSound = new FadeOutLoopingSoundInstance(SoundRegistry.DEAD_KING_MUSIC_INTRO.get(), SOUND_SOURCE, false);
        firstPhaseMelody = new FadeOutLoopingSoundInstance(SoundRegistry.DEAD_KING_FIRST_PHASE_MELODY.get(), SOUND_SOURCE, true);
        firstPhaseAccent = new FadeOutLoopingSoundInstance(SoundRegistry.DEAD_KING_FIRST_PHASE_ACCENT_01.get(), SOUND_SOURCE, false);
        firstPhaseDrums = new FadeOutLoopingSoundInstance(SoundRegistry.DEAD_KING_DRUM_LOOP.get(), SOUND_SOURCE, false);
        init();
    }

    private void init() {
        soundManager.stop(null, SoundSource.MUSIC);
        soundManager.play(beginSound);
    }

    public void tick() {
        if (boss.isDeadOrDying()) {
            //TODO: play death music?
            triggerStop();
            return;
        }
        if (stage == 0 && !soundManager.isActive(beginSound)) {
            stage++;
            initFirstPhaseLoop();
        } else if (stage == 1 && lastMilisPlayed + FIRST_PHASE_MELODY_LENGTH_MILIS < System.currentTimeMillis()) {
            if (accentStage % 2 == 1) {
                playAccent(firstPhaseDrums);
            } else {
                playAccent(firstPhaseAccent);
            }
        }
    }

    private void addLayer(FadeOutLoopingSoundInstance soundInstance) {
        soundManager.play(soundInstance);
        layers.add(soundInstance);
    }

    private void playAccent(FadeOutLoopingSoundInstance soundInstance) {
        accentStage++;
        lastMilisPlayed = System.currentTimeMillis();
        addLayer(soundInstance);
    }

    public void triggerStop() {
        beginSound.triggerStop();
        firstPhaseMelody.triggerStop();
        layers.forEach(FadeOutLoopingSoundInstance::triggerStop);
    }

    private void initFirstPhaseLoop() {
        soundManager.play(firstPhaseMelody);
        addLayer(firstPhaseDrums);
        playAccent(firstPhaseAccent);
    }
}
