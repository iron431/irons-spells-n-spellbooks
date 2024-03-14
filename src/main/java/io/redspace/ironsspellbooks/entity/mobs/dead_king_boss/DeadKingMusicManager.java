package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

public class DeadKingMusicManager {
    static final SoundSource SOUND_SOURCE = SoundSource.RECORDS;
    final DeadKingBoss boss;
    final SoundManager soundManager;
    FadeOutLoopingSoundInstance beginSound;
    FadeOutLoopingSoundInstance firstPhaseLoop;
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
        firstPhaseLoop = new FadeOutLoopingSoundInstance(SoundRegistry.DEAD_KING_DRUM_LOOP.get(), SOUND_SOURCE, true);
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
            soundManager.play(firstPhaseLoop);
        }
    }

    public void triggerStop() {
        beginSound.triggerStop();
        firstPhaseLoop.triggerStop();
    }
}
