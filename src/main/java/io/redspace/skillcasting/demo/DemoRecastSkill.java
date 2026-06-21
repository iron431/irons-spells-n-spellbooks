package io.redspace.skillcasting.demo;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class DemoRecastSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public int getCastTimeTicks() {
        return 20;
    }

    @Override
    public int getCooldownTicks() {
        return 40;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 origin = castContext.position();
        Vec3 direction = castContext.direction();
        int skillLevel = castContext.getSkillLevel();
        float velocity = 2.0f + 0.15f * skillLevel;

        for (int i = 0; i < 15; i++) {
            Vec3 random = new Vec3(Math.random(), Math.random(), Math.random()).subtract(0.5, 0.5, 0.5);
            Arrow arrow = new Arrow(EntityType.ARROW, level);
            if (castContext.caster().get() instanceof LivingEntity livingEntity) {
                arrow.setOwner(livingEntity);
            }
            arrow.setPos(origin.x, origin.y, origin.z);
            Vec3 vec3 = direction.add(random.scale(0.25f));
            arrow.shoot(vec3.x, vec3.y, vec3.z, velocity, 2.0f);
            level.addFreshEntity(arrow);
        }
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(3, 60));
    }
}
