package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Optional;

/**
 * Deprecated wrapper class for 1.20.1 compat. Used {@link io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData} instead
 */
@Deprecated(forRemoval = true)
public class AttackAnimationData extends io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData {
    /**
     * API Bridge. These values are meaningless!!!!
     */
    @Deprecated(forRemoval = true)
    public final int[] attackTimestamps;
    /**
     * API Bridge. These values are meaningless!!!!
     */
    @Deprecated(forRemoval = true)
    public final int lengthInTicks;
    /**
     * API Bridge. These values are meaningless!!!!
     */
    @Deprecated(forRemoval = true)
    public final String animationId;

    public AttackAnimationData(int lengthInTicks, String animationId, int... attackTimestamps) {
        super(lengthInTicks, animationId, attackTimestamps);
        this.attackTimestamps = attackTimestamps;
        this.animationId = animationId;
        this.lengthInTicks = lengthInTicks;
    }

    public AttackAnimationData(String animationId, int lengthInTicks, boolean canCancel, Optional<Float> areaAttackThreshold, Int2ObjectOpenHashMap<AttackKeyframe> attacks) {
        super(animationId, lengthInTicks, canCancel, areaAttackThreshold, attacks);
        this.attackTimestamps = attacks.keySet().toArray(new int[0]);
        this.animationId = animationId;
        this.lengthInTicks = lengthInTicks;
    }

    public AttackAnimationData(String animationId, int lengthInTicks, boolean canCancel, Optional<Float> areaAttackThreshold, Int2ObjectOpenHashMap<AttackKeyframe> attacks, float rangeMultiplier) {
        super(animationId, lengthInTicks, canCancel, areaAttackThreshold, attacks, rangeMultiplier);
        this.attackTimestamps = attacks.keySet().toArray(new int[0]);
        this.animationId = animationId;
        this.lengthInTicks = lengthInTicks;
    }
}
