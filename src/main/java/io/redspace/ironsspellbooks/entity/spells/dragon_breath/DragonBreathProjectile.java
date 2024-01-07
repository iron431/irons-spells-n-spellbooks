package io.redspace.ironsspellbooks.entity.spells.dragon_breath;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonBreathProjectile extends AbstractConeProjectile {
    public DragonBreathProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DragonBreathProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.DRAGON_BREATH_PROJECTILE.get(), level, entity);
    }

    @Override
    public void spawnParticles() {
        var owner = getOwner();
        if (!level().isClientSide || owner == null) {
            return;
        }
        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation.scale(1.6));

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .9f;
        double z = pos.z;

        double speed = random.nextDouble() * .35 + .25;
        for (int i = 0; i < 12; i++) {
            double offset = .15;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = .3;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level().addParticle(ParticleTypes.DRAGON_BREATH, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (DamageSources.applyDamage(entity, damage, SpellRegistry.DRAGON_BREATH_SPELL.get().getDamageSource(this, getOwner()))) {
            if (random.nextFloat() < .3f)
                createDragonBreathPuddle(entity.position());
        }

    }

    private void createDragonBreathPuddle(Vec3 pos) {
//        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
//        Entity entity = this.getOwner();
//        if (entity instanceof LivingEntity) {
//            areaeffectcloud.setOwner((LivingEntity) entity);
//        }
//
//        areaeffectcloud.setParticle(ParticleTypes.DRAGON_BREATH);
//        areaeffectcloud.setRadius(2.0F);
//        areaeffectcloud.setDuration(80);
//        areaeffectcloud.setRadiusPerTick(-.2f * ((7.0F - areaeffectcloud.getRadius()) / (float) areaeffectcloud.getDuration()));
//        areaeffectcloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, (int) (damage / 5)));
        DragonBreathPool pool = new DragonBreathPool(level());
        pool.setOwner(getOwner());
        pool.setDamage(this.damage);
        pool.moveTo(pos);
        this.level().addFreshEntity(pool);

    }
}
