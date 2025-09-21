package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.network.SyncAnimationPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.world.entity.Entity;

public interface IAnimatedAttacker {
    void playAnimation(String animationId);

    default <T extends Entity & IAnimatedAttacker> void serverTriggerAnimation(String animationId) {
        PacketDistributor.sendToPlayersTrackingEntity((T) this, new SyncAnimationPacket<>(animationId, (T) this));
    }
}
