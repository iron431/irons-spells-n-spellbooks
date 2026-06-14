package io.redspace.skillcasting.demo;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DemoInstantSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
        int skillLevel = castContext.get(SkillcastingComponentTypes.SKILL_LEVEL);

        SmallFireball snowball = new SmallFireball(castContext.level(), origin.x, origin.y, origin.z, direction);
        if (castContext.caster().get() instanceof LivingEntity livingEntity) {
            snowball.setOwner(livingEntity);
        }
        level.addFreshEntity(snowball);
    }
}
