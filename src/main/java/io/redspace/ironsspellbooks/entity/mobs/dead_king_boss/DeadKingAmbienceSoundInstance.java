package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

public class DeadKingAmbienceSoundInstance extends AbstractTickableSoundInstance {
    public static final int rangeSqr = 48 * 48;
    public static final int maxVolumeRangeSqr = 18 * 18;
    private static final float END_TRANSITION_TIME = 1f / 100;
    final DeadKingCorpseEntity entity;
    boolean ending = false;
    boolean triggerEnd = false;
    int tickCount;

    protected DeadKingAmbienceSoundInstance(DeadKingCorpseEntity entity) {
        super(SoundRegistry.DEAD_KING_AMBIENCE.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.attenuation = Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = 0;
        this.entity = entity;
    }

    @Override
    public void tick() {
        tickCount++;
        if (triggerEnd || entity.triggered()) {
            if (!ending) {
                ending = true;
            }
            this.volume -= END_TRANSITION_TIME;
        } else {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                var d = player.distanceToSqr(entity);
                this.volume = 1f - (float) Mth.clamp((d - maxVolumeRangeSqr) / (rangeSqr), 0, 1f);
            });

        }
        if (this.tickCount % 10 == 0) {
            //TODO: remove this and tickCount
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("v:" + getVolume()));
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
