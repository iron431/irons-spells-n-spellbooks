package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;

//https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
//https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
//https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
//https://github.com/maximumpower55/Aura

/*
Projectile.lerpMotion is also a decent reference
This method appears to set the movement delta (change in position) of the projectile, as well as its rotation based on the movement delta.
The method takes three parameters: p37279, p37280, and p37281, which represent the movement delta in the x, y, and z directions, respectively.
The method first sets the movement delta using the setDeltaMovement method. It then checks if the xRotO and yRotO fields are equal to 0.0. If they are,
it calculates the pitch and yaw (rotation around the x and y axes, respectively) based on the movement delta using the atan2 function. It then sets the
pitch and yaw of the projectile using the setXRot and setYRot methods, and stores the pitch and yaw in the xRotO and yRotO fields, respectively. Finally,
it calls the moveTo method to update the position and rotation of the projectile.
 */

public class ConeOfColdProjectile extends Projectile implements ItemSupplier {
    private static final int EXPIRE_TIME = 10 * 20;
    private int age;
    private float damage;
    private int tickCount;
    boolean didRun = false;

    private final ConeOfColdPart[] subEntities;

    public ConeOfColdProjectile(EntityType<? extends ConeOfColdProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);

        this.subEntities = new ConeOfColdPart[]{
                new ConeOfColdPart(this, "part1", 1.0F, 1.0F),
                new ConeOfColdPart(this, "part2", 2.5F, 1.5F),
                new ConeOfColdPart(this, "part3", 3.5F, 2.0F),
                new ConeOfColdPart(this, "part4", 4.5F, 3.0F)
        };

        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
    }

    public ConeOfColdPart[] getSubEntities() {
        return this.subEntities;
    }

    private float rotWrap(double p_31165_) {
        return (float) Mth.wrapDegrees(p_31165_);
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

    public ConeOfColdProjectile(Level level, LivingEntity entity) {
        this(EntityRegistry.CONE_OF_COLD_PROJECTILE.get(), level);
        setOwner(entity);
    }

    public void shoot(Vec3 rotation) {
        //setDeltaMovement(rotation.scale(SPEED));

    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        TestMod.LOGGER.info("MagicMissileProjectile.onHitBlock");
        kill();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        TestMod.LOGGER.info("MagicMissileProjectile.onHitEntity");
        if (entityHitResult.getEntity() instanceof LivingEntity target)
            //TODO: deal with the damage
            target.hurt(DamageSource.MAGIC, damage);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    private void tickPart(ConeOfColdPart coneOfColdPart, double x, double y, double z) {
        coneOfColdPart.setPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    protected static Vec3 rayTrace(Entity owner) {
        float f = owner.getXRot();
        float f1 = owner.getYRot();
        //Vec3 vector3d = owner.getEyePosition(1.0F);
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

        //TODO: try this instead of the ray trace
        /*
        So. This is what vectors are for.
        The player has a vector that is their "front" called "LookVec" (Search the EntityPlayer class).
        Take that vector, multiply by 0.5 (or 0.2 or whatever), add their current position, and voila. You have the spot a half-block in front of them.
        */

        super.tick();

        if (++age > EXPIRE_TIME) {
            discard();
            return;
        }

//        if (didRun) {
//            return;
//        }
//        didRun = true;

        var owner = this.getOwner();
        if (owner != null) {
            var rayTraceVector = rayTrace(owner);
            var ownerEyePos = owner.getEyePosition(1.0f).subtract(0, .8, 0);
            this.setPos(ownerEyePos);

            double scale = 1;

            for (int i = 0; i < subEntities.length; i++) {
                var subEntity = subEntities[i];

                double distance = 1 + (i * scale * subEntity.getDimensions(null).width / 2);
                var newVector = ownerEyePos.add(rayTraceVector.multiply(distance, distance, distance));
                subEntity.setPos(newVector);
                var vec3 = new Vec3(subEntity.getX(), subEntity.getY(), subEntity.getZ());
                subEntity.xo = vec3.x;
                subEntity.yo = vec3.y;
                subEntity.zo = vec3.z;
                subEntity.xOld = vec3.x;
                subEntity.yOld = vec3.y;
                subEntity.zOld = vec3.z;
            }
        }

//        if (++tickCount % 20 == 0) {
//            var la = owner.getLookAngle();
//            TestMod.LOGGER.info("ConeOfCold Owner look angle: x:{}, y:{}, z{}", la.x, la.y, la.z);
//            TestMod.LOGGER.info("ConeOfCold Position: x:{}, y:{}, z{}", this.getX(), this.getY(), this.getZ());
//            for (ConeOfColdPart subEntity : this.subEntities) {
//                TestMod.LOGGER.info("ConeOfCold part Position: x:{}, y:{}, z{}", subEntity.getX(), subEntity.getY(), subEntity.getZ());
//            }
//        }


//        Vec3[] vec3 = new Vec3[this.subEntities.length];
//        for (int j = 0; j < this.subEntities.length; ++j) {
//            vec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
//        }

//        if (!level.isClientSide) {
//            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
//            if (hitresult.getType() == HitResult.Type.ENTITY) {
//                onHitEntity((EntityHitResult) hitresult);
//            }
//
//            //pawnParticles();
//        }
//        Vec3[] vec3 = new Vec3[this.subEntities.length];
//        for(int j = 0; j < this.subEntities.length; ++j) {
//            vec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
//        }


//        for (int l = 0; l < this.subEntities.length; ++l) {
//            this.subEntities[l].xo = vec3[l].x;
//            this.subEntities[l].yo = vec3[l].y;
//            this.subEntities[l].zo = vec3[l].z;
//            this.subEntities[l].xOld = vec3[l].x;
//            this.subEntities[l].yOld = vec3[l].y;
//            this.subEntities[l].zOld = vec3[l].z;
//        }
        //setPos(position().add(getDeltaMovement()));
    }

    public void spawnParticles() {
        if (!level.isClientSide) {
            for (int count = 0; count < 3; count++) {
                double x = getX() + (level.random.nextInt(3) - 1) / 4D;
                double y = getY() + 0.2F + (level.random.nextInt(3) - 1) / 4D;
                double z = getZ() + (level.random.nextInt(3) - 1) / 4D;
                double deltaX = (level.random.nextInt(3) - 1) * level.random.nextDouble();
                double deltaY = (level.random.nextInt(3) - 1) * level.random.nextDouble();
                double deltaZ = (level.random.nextInt(3) - 1) * level.random.nextDouble();

                level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, ParticleTypes.END_ROD, true, x, y, z, 1, deltaX, deltaY, deltaZ, .1d));
            }
        }
    }

    /*
	@Override
	public void lerpMotion(double d, double e, double f) {
		super.lerpMotion(d, e, f);
		age = 0;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 10.0;
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * getViewScale();
		return d < e * e;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putShort("Age", (short)age);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		age = tag.getShort("Age");
	}
     */


}
