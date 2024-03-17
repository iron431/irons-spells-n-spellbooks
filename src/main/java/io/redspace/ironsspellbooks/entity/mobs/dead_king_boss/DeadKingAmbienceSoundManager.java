package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;


import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DeadKingAmbienceSoundManager {

    private final Vec3 vec3;
    @OnlyIn(Dist.CLIENT)
    private DeadKingAmbienceSoundInstance soundInstance;

    protected DeadKingAmbienceSoundManager(DeadKingCorpseEntity entity) {
        this.vec3 = entity.position();
    }

    public void trigger() {
        if (this.soundInstance == null || this.soundInstance.isStopped()) {
            this.soundInstance = new DeadKingAmbienceSoundInstance(vec3);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
        }
    }

    public void triggerStop(){
        soundInstance.triggerStop();
    }
}
