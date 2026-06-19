package io.redspace.skillcasting.demo;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DemoProjectileSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public int getCastTimeTicks() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 30;
    }

    @Override
    public void onCast(CastContext castContext) {
        Vec3 origin = castContext.position();
        Vec3 direction = castContext.direction();
        Level level = castContext.level();
        int skillLevel = castContext.getSkillLevel();
        double speed = 0.6 + 0.1 * skillLevel;

        Snowball snowball = new Snowball(castContext.level(), origin.x, origin.y, origin.z);
        snowball.setDeltaMovement(direction.scale(speed));
        if (castContext.caster().get() instanceof LivingEntity livingEntity) {
            snowball.setOwner(livingEntity);
        }
        level.addFreshEntity(snowball);
    }
}
