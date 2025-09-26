package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FocusOnTradingPlayerGoal<T extends PathfinderMob & IMerchantWizard> extends Goal {
    final T mob;
    int hurtTracker;

    public FocusOnTradingPlayerGoal(T mob) {
        // Use move flag to override movement goals, and stand still
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return mob.getTradingPlayer() != null;
    }

    @Override
    public void start() {
        super.start();
        mob.getNavigation().stop();
        hurtTracker = mob.getLastHurtByMobTimestamp();
    }

    @Override
    public void tick() {
        var player = mob.getTradingPlayer();
        if (player == null || player.isDeadOrDying() || player.isRemoved()) {
            stop();
            return;
        }
        this.mob.getLookControl().setLookAt(player);
        if (mob.getLastHurtByMobTimestamp() != hurtTracker) {
            mob.stopTrading();
            stop();
        }
    }
}
