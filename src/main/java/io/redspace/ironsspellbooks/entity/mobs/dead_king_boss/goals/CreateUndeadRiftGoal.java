package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.undead_spawner.UndeadRiftEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class CreateUndeadRiftGoal extends Goal {
    final DeadKingBoss mob;
    int cooldown;

    public CreateUndeadRiftGoal(DeadKingBoss mob) {
        this.mob = mob;
        cooldown = 20 * 10;
    }

    @Override
    public boolean canUse() {
        return mob.isAggressive() && mob.isOminous() && --cooldown <= 0 && checkSummonCountOrResetCooldown();
    }

    private boolean checkSummonCountOrResetCooldown() {
        int summons = mob.level.getEntities(mob, mob.getBoundingBox().inflate(32), entity -> SummonManager.getOwner(entity) == mob).size();
        if (summons < 12) {
            return true;
        } else {
            cooldown = 20 * 5;
            return false;
        }
    }

    @Override
    public void start() {
        Level level = mob.level;
        Entity target = mob.getTarget();
        if (target == null) {
            // should be impossible
            return;
        }
        List<? extends Entity> otherTargets = level.getEntitiesOfClass(target.getClass(), mob.getBoundingBox().inflate(20, 10, 20), entity -> !entity.isSpectator() && Utils.hasLineOfSight(level, mob, entity, false));
        if (!otherTargets.isEmpty()) {
            target = otherTargets.get(mob.getRandom().nextInt(otherTargets.size()));
        }

        int summonCount = 5 + (int) Mth.lerp(1 - mob.getHealth() / mob.getMaxHealth(), 0, 5 + 1);
        int delay = 30;
        this.cooldown = summonCount * delay * 3;

        UndeadRiftEntity rift = new UndeadRiftEntity(level);
        SummonManager.setOwner(rift, mob);
        rift.setDelay(delay);
        rift.setSummonsToSpawn(summonCount);
        Vec3 dir = new Vec3(0, 0, 1).yRot(mob.getRandom().nextFloat() * Mth.TWO_PI).add(0, -.2, 0).normalize();
        Vec3 targetPos = level.clip(new ClipContext(
                target.getBoundingBox().getCenter(), target.getBoundingBox().getCenter().add(dir.scale(6 + target.getBbWidth())),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty())).getLocation();
        targetPos = Utils.moveToRelativeGroundLevel(level, targetPos.subtract(dir), 3, 10);
        rift.moveTo(targetPos);
        rift.setYRot(Utils.getAngle(targetPos.x, targetPos.z, mob.getX(), mob.getZ()) * Mth.RAD_TO_DEG + 90);
        rift.setForcedTarget((LivingEntity) target); // safe cast because the entities of class must extend living entity by proxy of getTarget returning a living entity
        level.addFreshEntity(rift);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
