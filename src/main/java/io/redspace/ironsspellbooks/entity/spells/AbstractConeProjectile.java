package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.entity.NoKnockbackProjectile;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractConeProjectile extends Projectile implements NoKnockbackProjectile {
    protected static final int FAILSAFE_EXPIRE_TIME = 20 * 20;
    protected int age;
    protected float damage;
    protected boolean dealDamageActive = true;
    protected final ConePart[] subEntities;

    public AbstractConeProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level, LivingEntity entity) {
        this(entityType, level);
        setOwner(entity);
    }

    public AbstractConeProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.blocksBuilding = false;

        //TODO: dynamically generate cone parts based off of input for overall cone length/width
        this.subEntities = new ConePart[]{
                new ConePart(this, "part1", 1.0F, 1.0F),
                new ConePart(this, "part2", 2.5F, 1.5F),
                new ConePart(this, "part3", 3.5F, 2.0F),
                new ConePart(this, "part4", 4.5F, 3.0F)
        };
        //Ironsspellbooks.logger.debug("AbstractConeProjectile: Creating sub-entities");

        //this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public abstract void spawnParticles();

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected abstract void onHitEntity(EntityHitResult entityHitResult);

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
            this.setXRot(owner.getXRot());
            this.setYRot(owner.getYRot());
            this.yRotO = getYRot();
            this.xRotO = getXRot();
            //setDeltaMovement(ownerEyePos);

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
        if (!level().isClientSide) {
            if (dealDamageActive) {
                for (Entity entity : getSubEntityCollisions()) {
                    //irons_spellbooks.LOGGER.debug("ConeOfColdHit : {}", entity.getName().getString());
                    onHitEntity(new EntityHitResult(entity));
                }
                dealDamageActive = false;
            }
        } else {
            spawnParticles();
        }

    }

    public void setDealDamageActive() {
        this.dealDamageActive = true;
    }

    protected Set<Entity> getSubEntityCollisions() {
        List<Entity> collisions = new ArrayList<>();
        for (Entity conepart : subEntities) {
            collisions.addAll(level().getEntities(conepart, conepart.getBoundingBox()));
        }

        return collisions.stream().filter(target ->
                target != getOwner() && target instanceof LivingEntity && hasLineOfSight(this, target)
        ).collect(Collectors.toSet());
    }

    protected static boolean hasLineOfSight(Entity start, Entity target) {
        Vec3 vec3 = new Vec3(start.getX(), start.getEyeY(), start.getZ());
        Vec3 vec31 = new Vec3(target.getX(), target.getEyeY(), target.getZ());

        boolean isShieldBlockingLOS = Utils.raycastForEntity(start.level(), start, vec3, vec31, false, 0, (entity) -> entity instanceof ShieldEntity).getType() == HitResult.Type.ENTITY;
        return !isShieldBlockingLOS && start.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, start)).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.damage = pCompound.getFloat("Damage");
    }
}
