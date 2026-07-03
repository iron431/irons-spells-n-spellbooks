package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.LivingEntity;

public class EchoingStrikesData {
    private int hitCount;
    public int vfxTimestamp;

    public EchoingStrikesData(int hitsRemaining) {
        this.hitCount = hitsRemaining;
    }

//    public EchoingStrikesData(IAttachmentHolder holder) {
//
//    }

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
        return ((MagicData.IExtendedEntity) entity).irons_spellbooks$getEchoingStrikesData();
    }
    public static boolean has(LivingEntity entity) {
        return ((MagicData.IExtendedEntity) entity).irons_spellbooks$hasEchoingStrikesData();
    }

    public static void remove(LivingEntity livingEntity) {
        ((MagicData.IExtendedEntity) livingEntity).irons_spellbooks$removeEchoingStrikesData();
    }
}
