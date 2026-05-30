package io.redspace.skillcasting.demo;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DemoContinuousArrowsSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public int getCastTimeTicks() {
        return 60;
    }

    @Override
    public int continuousInterval() {
        return 4;
    }

    @Override
    public int getCooldownTicks() {
        return 40;
    }

    @Override
    public void onCast(CastContext castContext) {
        Vec3 origin = castContext.position();
        Vec3 direction = castContext.direction();
        Level level = castContext.level();
        int skillLevel = castContext.get(SkillcastingComponentTypes.SKILL_LEVEL.get());
        float velocity = 2.0f + 0.15f * skillLevel;

        if (!(castContext.caster().get() instanceof LivingEntity owner)) {
            return;
        }
        Arrow arrow = new Arrow(EntityType.ARROW, level);
        arrow.setOwner(owner);
        arrow.setPos(origin.x, origin.y, origin.z);
        arrow.shoot(direction.x, direction.y, direction.z, velocity, 2.0f);
        level.addFreshEntity(arrow);
    }
}
