package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb.OminousFireOrbEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OminousSpawnFireOrbGoal extends Goal {
    private static final int FUSE_TICKS = 25 * 20;
    private static final int DELAY_TICKS = 0;
    private static final int BASE_COOLDOWN_TICKS = FUSE_TICKS + DELAY_TICKS;
    FireBossEntity mob;
    int cooldown = 100;

    public OminousSpawnFireOrbGoal(FireBossEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (mob.isOminous()) {
            cooldown -= 2;
            return cooldown <= 0 && mob.getTarget() != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        super.start();
        if (mob.level.getEntitiesOfClass(OminousFireOrbEntity.class, AABB.ofSize(mob.position(), 32, 32, 32)).size() >= 1) {
            // prevent duplicate orbs (lazily)
            cooldown = mob.getRandom().nextIntBetweenInclusive(7, 13) * 20;
            return;
        }
        Vec3 pos = mob.getSpawnPos();
        if (pos == null) {
            pos = mob.position();
        }
        cooldown = BASE_COOLDOWN_TICKS + mob.getRandom().nextIntBetweenInclusive(7, 13) * 20;
        var target = mob.getTarget();
        if (target != null) {
            mob.playSound(SoundRegistry.FIRE_BOSS_FIREBALL.get(), 3f, Utils.random.nextIntBetweenInclusive(80, 110) * .01f);
            mob.playSound(SoundRegistry.SOULCALLER_TOLL_SUCCESS.get(), 3f, .75f);
            MagicManager.spawnParticles(mob.level, new BlastwaveParticleOptions(.3f, .4f, 1f, 24), pos.x, pos.y + 1, pos.z, 0, 0, 0, 0, 0, true);

            float intensity = Mth.lerp(mob.getHealth() / mob.getMaxHealth(), 1, 0.75f);
            OminousFireOrbEntity fireOrb = new OminousFireOrbEntity(mob.level);
            fireOrb.setFuse(FUSE_TICKS);
            fireOrb.setChargeTime(DELAY_TICKS);
            fireOrb.setDamage(160 * intensity);
            fireOrb.setHealth(150);
            fireOrb.setRadius(50 * intensity);
            fireOrb.setOwner(mob);
            fireOrb.setPos(pos);
            mob.level.addFreshEntity(fireOrb);
        }
    }
}
