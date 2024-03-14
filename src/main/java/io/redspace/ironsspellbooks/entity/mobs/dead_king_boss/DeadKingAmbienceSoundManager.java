package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DeadKingAmbienceSoundManager {

    private final DeadKingCorpseEntity entity;
    @OnlyIn(Dist.CLIENT)
    private DeadKingAmbienceSoundInstance soundInstance;

    protected DeadKingAmbienceSoundManager(DeadKingCorpseEntity entity) {
        this.entity = entity;
    }

    public void trigger() {
        if (this.soundInstance == null || this.soundInstance.isStopped()) {
            this.soundInstance = new DeadKingAmbienceSoundInstance(entity);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
        }
    }
}
