package io.redspace.ironsspellbooks.entity.spells.cone_of_cold;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ConeOfColdProjectile extends AbstractConeProjectile {
    public ConeOfColdProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level){
        super(entityType,level);
    }

    public ConeOfColdProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.CONE_OF_COLD_PROJECTILE.get(), level, entity);
    }


    @Override
    public void spawnParticles() {
        var owner = getOwner();
        if (!level.isClientSide || owner == null) {
            return;
        }
        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation.scale(1.6));

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .9f;
        double z = pos.z;

        double speed = random.nextDouble() * .4 + .45;
        for (int i = 0; i < 25; i++) {
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = .8;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random()  * 2 * angularness - angularness, Math.random()  * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(Math.random() > .05 ? ParticleTypes.SNOWFLAKE : ParticleHelper.SNOWFLAKE, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }


    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        //irons_spellbooks.LOGGER.debug("ConeOfColdProjectile.onHitEntity: {}", entityHitResult.getEntity().getName().getString());
        var entity = entityHitResult.getEntity();
        if (DamageSources.applyDamage(entity, damage, SpellType.CONE_OF_COLD_SPELL.getDamageSource(this, getOwner()), SchoolType.ICE)  && !level.isClientSide && entity.canFreeze())
            entity.setTicksFrozen(entity.getTicksFrozen() + 80);
    }

}
