package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DeadKingAmbienceSoundInstance extends AbstractTickableSoundInstance {
    public static final int SOUND_RANGE_SQR = 20 * 20;
    public static final int MAX_VOLUME_RANGE_SQR = 12 * 12;
    private static final float END_TRANSITION_TIME = 1f / 100;
    final Vec3 vec3;
    boolean ending = false;
    boolean triggerEnd = false;

    protected DeadKingAmbienceSoundInstance(Vec3 vec3) {
        super(SoundRegistry.DEAD_KING_AMBIENCE.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.attenuation = Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = 0;
        this.vec3 = vec3;
    }

    @Override
    public void tick() {
        if (triggerEnd) {
            if (!ending) {
                ending = true;
            }
            this.volume -= END_TRANSITION_TIME;
        } else {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                var d = player.distanceToSqr(vec3);
                this.volume = 1f - (float) Mth.clamp((d - MAX_VOLUME_RANGE_SQR) / (SOUND_RANGE_SQR), 0, 1f);
            });

        }

        if (volume <= 0) {
            this.stop();
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
