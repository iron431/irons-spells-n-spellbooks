package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.SupportMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.function.Predicate;

public class FindSupportableTargetGoal<M extends Mob & SupportMob> extends NearestAttackableTargetGoal<LivingEntity> {
    SupportMob supportMob;

    public FindSupportableTargetGoal(M pMob, Class pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pTargetType, 5, pMustSee, false, pTargetPredicate);
        this.supportMob = pMob;
    }

    @Override
    public void start() {
        //IronsSpellbooks.LOGGER.debug("FindSupportableTargetGoal starting: {}", this.target.getName().getString());
        super.start();
        this.supportMob.setSupportTarget(this.target);
        this.mob.setTarget(null);
    }
}
