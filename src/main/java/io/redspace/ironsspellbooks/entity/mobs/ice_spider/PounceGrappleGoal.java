package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class PounceGrappleGoal extends AnimatedActionGoal<IceSpiderEntity> {
    private static final AttributeModifier TELEGRAPH_SPEED_MODIFIER = new AttributeModifier(IronsSpellbooks.id("pouncing"), -0.40, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final int DAMAGER_START = 30;
    private static final int DAMAGER_END = 35;

    public PounceGrappleGoal(IceSpiderEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return !mob.isCrouching() && mob.getTarget() != null && (Utils.random.nextFloat() < 0.05 || mob.distanceToSqr(mob.getTarget()) > 5 * 5);
    }

    @Override
    protected int getActionTimestamp() {
        return 20;
    }

    @Override
    protected int getActionDuration() {
        return 40;
    }

    @Override
    protected int getCooldown() {
        return mob.getRandom().nextIntBetweenInclusive(3, 8) * 20;
    }

    @Override
    protected String getAnimationId() {
        return "attack_grapple_pounce";
    }

    @Override
    public void tick() {
        super.tick();
        if (abilityTimer >= DAMAGER_START && abilityTimer <= DAMAGER_END) {
            var target = mob.getTarget();
            if (target == null) {
                return;
            }
            double meleeRange = mob.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) * mob.getScale();
            if (target.distanceToSqr(mob) <= meleeRange * meleeRange && Utils.hasLineOfSight(mob.level, mob, target, true)) {
                if (this.mob.doHurtTarget(target)) {
                    mob.startGrapple(target);
                }
                stop(); // only allow one chance for attack
            }
        }
    }

    @Override
    public boolean isInterruptable() {
        // we are messing with attributes, so it is required that the full extent of logic plays out to correctly manage the state tracking
        return false;
    }

    @Override
    protected void doAction() {
        // perform leap
        //todo: sound effect
        Vec3 leapVector = new Vec3(0, .5, 1.5);
        var target = mob.getTarget();
        if (target == null) {
            return;
        }
        Vec3 power = Utils.lerp(Math.clamp(mob.distanceTo(target) / 18f, 0, 1), new Vec3(0.125, 0.25, 0.125), new Vec3(3, 1.2, 3));
        Vec3 lunge = leapVector.multiply(power.x, power.y, power.z).yRot(-Utils.getAngle(mob.getX(), mob.getZ(), target.getX(), target.getZ()) - Mth.HALF_PI);
        mob.push(lunge);
        mob.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(TELEGRAPH_SPEED_MODIFIER);
    }

    @Override
    public void start() {
        super.start();
        mob.getAttribute(Attributes.MOVEMENT_SPEED).addOrUpdateTransientModifier(TELEGRAPH_SPEED_MODIFIER);
    }
}
