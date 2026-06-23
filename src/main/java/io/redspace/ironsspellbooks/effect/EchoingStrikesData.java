package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class EchoingStrikesData {
    private int hitCount;

    public EchoingStrikesData(int hitsRemaining) {
        this.hitCount = hitsRemaining;
    }

    public EchoingStrikesData(IAttachmentHolder holder) {

    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public boolean hasHitsRemaining() {
        return hitCount > 0;
    }

    public void decrementHit() {
        if (hitCount > 0) {
            hitCount--;
        }
    }

    public void clear() {
        hitCount = 0;
    }

    public static EchoingStrikesData get(LivingEntity entity) {
        return entity.getData(DataAttachmentRegistry.ECHOING_STRIKES_DATA);
    }

    public static void remove(LivingEntity livingEntity) {
        livingEntity.removeData(DataAttachmentRegistry.ECHOING_STRIKES_DATA);
    }
}
