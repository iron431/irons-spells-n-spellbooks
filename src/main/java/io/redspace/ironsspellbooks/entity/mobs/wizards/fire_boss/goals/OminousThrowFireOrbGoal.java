package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb.OminousFireOrbEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class OminousThrowFireOrbGoal extends AnimatedActionGoal<FireBossEntity> {
    public static final int ANIM_DURATION = 20;
    public static final int ACTION_TIMESTAMP = 5;

    public OminousThrowFireOrbGoal(FireBossEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return mob.isOminous() && mob.getTarget() != null;
    }

    @Override
    protected int getActionTimestamp() {
        return ACTION_TIMESTAMP;
    }

    @Override
    protected int getActionDuration() {
        return ANIM_DURATION;
    }

    @Override
    protected int getCooldown() {
        return 20 * 10;
    }

    @Override
    protected String getAnimationId() {
        return "offhand_parry";
    }

    @Override
    protected void doAction() {
        var target = mob.getTarget();
        if (target != null) {
            mob.playSound(SoundRegistry.FIRE_BOSS_FIREBALL.get(), 3f, Utils.random.nextIntBetweenInclusive(80, 110) * .01f);
            mob.playSound(SoundRegistry.SOULCALLER_TOLL_SUCCESS.get(), 3f, .75f);
            MagicManager.spawnParticles(mob.level, new BlastwaveParticleOptions(1, .6f, 0.3f, 8), mob.getX(), mob.getBoundingBox().getCenter().y, mob.getZ(), 0, 0, 0, 0, 0, true);

            Vec3 delta = this.mob.position().subtract(target.position()).normalize();
            Vec3 random = Utils.getRandomVec3(1).normalize().subtract(delta).normalize();
            float intensity = Mth.lerp(mob.getHealth() / mob.getMaxHealth(), 1, 0.75f);
            OminousFireOrbEntity fireOrb = new OminousFireOrbEntity(mob.level);
            fireOrb.setFuse(20 * 25);
            fireOrb.setDamage(160 * intensity);
            fireOrb.setHealth(150);
            fireOrb.setRadius(50 * intensity);
            fireOrb.setOwner(mob);
            fireOrb.setPos(mob.getEyePosition());
            fireOrb.setDeltaMovement(random.scale(0.3).add(0, 1, 0));
            mob.level.addFreshEntity(fireOrb);
        }
    }
}
