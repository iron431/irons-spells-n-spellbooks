package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class GenericDefendVillageTargetGoal extends TargetGoal {
    private final Mob protector;
    @Nullable
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);

    public GenericDefendVillageTargetGoal(Mob mob) {
        super(mob, false, true);
        this.protector = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        AABB aabb = this.protector.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        List<Villager> list = this.protector.level.getNearbyEntities(Villager.class, this.attackTargeting, this.protector, aabb);
        List<Player> list1 = this.protector.level.getNearbyPlayers(this.attackTargeting, this.protector, aabb);

        for(Villager villager : list) {
            for(Player player : list1) {
                int i = villager.getPlayerReputation(player);
                if (i <= -100) {
                    this.potentialTarget = player;
                }
            }
        }

        if (this.potentialTarget == null) {
            return false;
        } else {
            return !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.protector.setTarget(this.potentialTarget);
        super.start();
    }
}
