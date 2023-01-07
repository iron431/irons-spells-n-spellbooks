package com.example.testmod.entity.cone_of_cold;

import com.example.testmod.TestMod;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConeOfColdProjectile extends Projectile {
    private static final int FAILSAFE_EXPIRE_TIME = 20 * 20;
    private int age;
    private float damage;
    private boolean dealDamageActive = true;
    private final ConeOfColdPart[] subEntities;

    public ConeOfColdProjectile(Level level, LivingEntity entity) {
        this(EntityRegistry.CONE_OF_COLD_PROJECTILE.get(), level);
        setOwner(entity);
    }

    public ConeOfColdProjectile(EntityType<? extends ConeOfColdProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);

        this.subEntities = new ConeOfColdPart[]{
                new ConeOfColdPart(this, "part1", 1.0F, 1.0F),
                new ConeOfColdPart(this, "part2", 2.5F, 1.5F),
                new ConeOfColdPart(this, "part3", 3.5F, 2.0F),
                new ConeOfColdPart(this, "part4", 4.5F, 3.0F)
        };

        //this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(id + i + 1);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        TestMod.LOGGER.debug("ConeOfColdProjectile.onHitEntity: {}", entityHitResult.getEntity().getName().getString());
        var entity = entityHitResult.getEntity();
        entity.hurt(DamageSource.MAGIC, damage);
        entity.setTicksFrozen(entity.getTicksFrozen() + 80);
    }

    @Override
    protected void defineSynchedData() {
    }

    protected static Vec3 rayTrace(Entity owner) {
        float f = owner.getXRot();
        float f1 = owner.getYRot();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        return new Vec3(f6, f5, f7);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 currentPos, Vec3 deltaPos, AABB aabb, Predicate<Entity> predicate) {
        TestMod.LOGGER.debug("ConeOfColdProjectile.getEntityHitResult.enter:");
        return getEntityHitResult(level, entity, currentPos, deltaPos, aabb, predicate, 0.3F);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 currentPos, Vec3 deltaPos, AABB aabbPassedin, Predicate<Entity> predicate, float inflateAmount) {
        double d0 = Double.MAX_VALUE;
        Entity hitEntity = null;

        for (Entity entityToCheck : level.getEntities(entity, aabbPassedin, predicate)) {
            TestMod.LOGGER.debug("ConeOfColdProjectile:getEntityHitResult.2: {}", entityToCheck.getName().getString());
            AABB aabb = entityToCheck.getBoundingBox().inflate((double) inflateAmount);
            Optional<Vec3> optional = aabb.clip(currentPos, deltaPos);
            if (optional.isPresent()) {
                double d1 = currentPos.distanceToSqr(optional.get());
                if (d1 < d0) {
                    hitEntity = entityToCheck;
                    d0 = d1;
                }
            }
        }

        return hitEntity == null ? null : new EntityHitResult(hitEntity);
    }

    @Override
    public void tick() {
        super.tick();

        if (++age > FAILSAFE_EXPIRE_TIME) {
            //This exists in case there is any bug with removing the cone onCastComplete
            discard();
        }

        //TODO: try this instead of the ray trace
        /*
        So. This is what vectors are for.
        The player has a vector that is their "front" called "LookVec" (Search the EntityPlayer class).
        Take that vector, multiply by 0.5 (or 0.2 or whatever), add their current position, and voila. You have the spot a half-block in front of them.
        */

        var owner = this.getOwner();
        if (owner != null) {
            var rayTraceVector = rayTrace(owner);
            var ownerEyePos = owner.getEyePosition(1.0f).subtract(0, .8, 0);
            this.setPos(ownerEyePos);
            setDeltaMovement(ownerEyePos);

            double scale = 1;

            for (int i = 0; i < subEntities.length; i++) {
                var subEntity = subEntities[i];

                double distance = 1 + (i * scale * subEntity.getDimensions(null).width / 2);
                Vec3 newVector = ownerEyePos.add(rayTraceVector.multiply(distance, distance, distance));
                subEntity.setPos(newVector);
                subEntity.setDeltaMovement(newVector);
                var vec3 = new Vec3(subEntity.getX(), subEntity.getY(), subEntity.getZ());
                subEntity.xo = vec3.x;
                subEntity.yo = vec3.y;
                subEntity.zo = vec3.z;
                subEntity.xOld = vec3.x;
                subEntity.yOld = vec3.y;
                subEntity.zOld = vec3.z;
            }
        }

        /* Hit Detection */
        if (!level.isClientSide) {
            if (dealDamageActive) {
                for (Entity entity : getSubEntityCollisions()) {
                    TestMod.LOGGER.debug("ConeOfColdHit : {}", entity.getName().getString());
                    onHitEntity(new EntityHitResult(entity));
                }
                dealDamageActive = false;
            }

            if (age % 10 == 0) {
                TestMod.LOGGER.debug("ConeOfCold Pos: {} {}", owner.position(), owner.getLookAngle());
            }

            //spawnParticles();
        } else {
            spawnParticles();
        }

    }

    public void setDealDamageActive() {
        this.dealDamageActive = true;
    }

    private Set<Entity> getSubEntityCollisions() {
        List<Entity> collisions = new ArrayList<>();
        for (Entity conepart : subEntities) {
            collisions.addAll(level.getEntities(conepart, conepart.getBoundingBox()));
        }

        return collisions.stream().filter(target ->
                target != getOwner() && target instanceof LivingEntity && hasLineOfSightOnlyClip(this, target)
        ).collect(Collectors.toSet());
    }

    public static boolean hasLineOfSightOnlyClip(Entity entity, Entity target) {
        Vec3 vec3 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 vec31 = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        return entity.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

//    public void spawnParticles() {
//        var owner = getOwner();
//        //This is right in front of the player
//        var vec3 = owner.getLookAngle().multiply(2, 2, 2).add(owner.position());
//
//        var vec3a = owner.getLookAngle().multiply(10, 10, 10);
//
//        double x = vec3.x;
//        double y = vec3.y;
//        double z = vec3.z;
//        double dx = -1; //vec3a.x;
//        double dy = 0; //vec3a.y;
//        double dz = 0; //vec3a.z;
//
//        for (int count = 0; count < 10; count++) {
//            ((ServerLevel)level).sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 1, dx, dy, dz, .2);
//        }
//    }

    public void spawnParticles() {
        if (!level.isClientSide)
            return;
        var owner = getOwner();
        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation.scale(0.5f));

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .8f;
        double z = pos.z;

        double speed = .6;
        for (int i = 0; i < 10; i++) {
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            Vec3 randomVec = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(Math.random() > .1 ? ParticleTypes.SNOWFLAKE : ParticleHelper.SNOWFLAKE, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }
    }
}
