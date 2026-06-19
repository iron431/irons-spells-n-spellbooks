package io.redspace.skillcasting.demo;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

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
        int skillLevel = castContext.getSkillLevel();
        float velocity = 2.0f + 0.15f * skillLevel;

        Arrow arrow = new Arrow(EntityType.ARROW, level);
        if (castContext.caster().get() instanceof LivingEntity livingEntity) {
            arrow.setOwner(livingEntity);
        }
        arrow.setPos(origin.x, origin.y, origin.z);
        arrow.shoot(direction.x, direction.y, direction.z, velocity, 2.0f);
        level.addFreshEntity(arrow);
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((caster, data, cast) -> {
            Vec3 pos = cast.context().position();
            Vec3 forward = cast.context().direction().scale(5).add(Utils.getRandomVec3(1)).normalize().scale(0.25);
            caster.level().addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, forward.x, forward.y, forward.z);
        });
    }
}
