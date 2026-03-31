package io.redspace.ironsspellbooks.entity.mobs.keeper.ability;

import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.MobAbilityType;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class KeeperLungeAbilityInstance extends KeeperMeleeAbilityInstance {
    public static final int LUNGE_START_FRAME = 56;
    public static final int LUNGE_END_FRAME = 64;
    boolean hasHitLunge;
    Vec3 lungeStartPos;

    public KeeperLungeAbilityInstance(MobAbilityType<KeeperEntity> type, KeeperEntity entity) {
        super(type, entity);
    }

    @Override
    public void tick() {
        super.tick();
        var target = entity.getTarget();
        if (target == null) {
            return;
        }
        if (currentTick == LUNGE_START_FRAME) {
            Vec3 lunge = target.position().subtract(entity.position()).normalize().multiply(2.4, .5, 2.4).add(0, 0.15, 0);
            entity.push(lunge.x, lunge.y, lunge.z);
            lungeStartPos = entity.position();
            entity.getNavigation().stop();
        }
        if (!hasHitLunge && currentTick >= LUNGE_START_FRAME && currentTick <= LUNGE_END_FRAME) {
            float meleeRange = (float) (entity.getAttributeValue(Attributes.SCALE) * entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
            if (!hasHitLunge && this.entity.distanceToSqr(target) <= meleeRange * meleeRange * .45f * 0.45f) {
                if (entity.doHurtTarget(target)) {
                    entity.playSound(SoundRegistry.KEEPER_SWORD_IMPACT.get(), 1, Mth.randomBetweenInclusive(entity.getRandom(), 9, 13) * .1f);
                }
                if (lungeStartPos != null) {
                    Vec3 knockback = lungeStartPos.subtract(target.position());
                    target.knockback(1, knockback.x, knockback.z);
                }
                hasHitLunge = true;
            }
        }
    }

    @Override
    protected void forceFaceTarget() {
        if (currentTick > LUNGE_START_FRAME) {
            return;
        }
        super.forceFaceTarget();
    }
}
