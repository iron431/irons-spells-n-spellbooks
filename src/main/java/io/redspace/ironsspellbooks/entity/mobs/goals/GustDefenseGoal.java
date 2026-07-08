package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;

public class GustDefenseGoal extends Goal {
    protected final PathfinderMob mob;
    protected int attackCooldown = 0;

    public GustDefenseGoal(Mob abstractSpellCastingMob) {
        if (abstractSpellCastingMob instanceof PathfinderMob m) {
            this.mob = m;
        } else
            throw new IllegalStateException("Unable to add " + this.getClass().getSimpleName() + "to entity, must extend PathfinderMob.");
    }

    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && --attackCooldown <= 0 && livingentity.isAlive() && shouldAreaAttack(livingentity)) {
            return false;
        } else {
            return false;
        }
    }

    public boolean shouldAreaAttack(LivingEntity livingEntity) {
        if (SkillcastingData.get(mob).isCasting()) {
            return false;
        }
        var d = livingEntity.distanceToSqr(mob);
        var inRange = d < 5 * 5;
        if (!inRange) {
            return false;
        }

        if (livingEntity.getType() == EntityType.VINDICATOR) {
            start();
            return false;
        }

        //anti-rush
        if (this.mob.getHealth() / this.mob.getMaxHealth() < .25f && mob.level.getEntities(mob, mob.getBoundingBox().inflate(3f), (entity -> entity instanceof Enemy)).size() > 1) {
            start();
            return false;
        }

        //swarm control
        int mobCount = livingEntity.level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(6f), (entity -> entity instanceof Enemy)).size();
        if (mobCount >= 2)
            start();
        return false;
    }

    @Override
    public void start() {
        //fixme: 2.5 second cooldown??
        this.attackCooldown = 40 + mob.getRandom().nextInt(30);
        int spellLevel = (int) (SkillRegistry.GUST_SPELL.get().getMaxLevel() * .5f);
        var spellType = SkillRegistry.GUST_SPELL.get();
        //fixme: spellcasting
//        spellCastingMob.initiateCastSpell(spellType, spellLevel);
    }
}
