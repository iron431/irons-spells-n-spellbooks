package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class CreateFangSwirlGoal extends Goal {
    final DeadKingBoss mob;
    int cooldown;

    public CreateFangSwirlGoal(DeadKingBoss mob) {
        this.mob = mob;
        cooldown = 20 * 10;
    }

    @Override
    public boolean canUse() {
        // only use in ominous mode combat
        if (!(mob.isAggressive() && mob.isOminous()) || mob.isCasting()) {
            return false;
        }
        // cooldown
        return --cooldown <= 0;
    }

    @Override
    public void start() {
        Level level = mob.level;
        Entity target = mob.getTarget();
        cooldown = 20 * 10;
        if (target == null) {
            return;
        }
        CastComponentMap componentMap = new CastComponentMap();
        componentMap.set(SkillcastingComponentTypes.TARGETED_ENTITIES.get(), new TargetedEntitiesData(target));
        mob.initiateCastSpell(SpellRegistry.FANG_SWIRL_SPELL.get(), (int) (8 * mob.getAttributeValue(AttributeRegistry.SUMMON_DAMAGE)), componentMap);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
