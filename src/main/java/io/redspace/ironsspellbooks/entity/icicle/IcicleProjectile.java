package io.redspace.ironsspellbooks.entity.icicle;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

//https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
//https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
//https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
//https://github.com/maximumpower55/Aura

public class IcicleProjectile extends Projectile implements ItemSupplier {
    private static final double SPEED = 2d;
    private static final int EXPIRE_TIME = 5 * 20;


    private int age;
    private float damage;

    public IcicleProjectile(EntityType<? extends IcicleProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public IcicleProjectile(EntityType<? extends IcicleProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType, levelIn);
        setOwner(shooter);
    }

    public IcicleProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.ICICLE_PROJECTILE.get(), levelIn, shooter);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(SPEED));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        if (age > EXPIRE_TIME) {
            discard();
            return;
        }
        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                onHit(hitresult);
            }
        } else {
            spawnParticles();
        }
        setPos(position().add(getDeltaMovement()));

        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }

        age++;
    }

    @Override
    protected void onHit(HitResult hitresult) {
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitresult);
        } else if (hitresult.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitresult);
        }
        double x = xOld;
        double y = yOld;
        double z = zOld;

        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, x, y, z, 15, .1, .1, .1, .1, true);
        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.ICE_IMPACT.get(), SoundSource.NEUTRAL, 1, .9f + level.random.nextFloat() * .2f);

    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        kill();

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        boolean hit = DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellType.ICICLE_SPELL.getDamageSource(this, getOwner()), SchoolType.ICE);
        if (hit && entityHitResult.getEntity() instanceof LivingEntity target) {
            target.setTicksFrozen(target.getTicksFrozen() + 165);
        }

    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    //https://forge.gemwire.uk/wiki/Particles
    public void spawnParticles() {

        for (int i = 0; i < 1; i++) {
            double speed = .05;
            double dx = level.random.nextDouble() * 2 * speed - speed;
            double dy = level.random.nextDouble() * 2 * speed - speed;
            double dz = level.random.nextDouble() * 2 * speed - speed;
            level.addParticle(level.random.nextDouble() < .3 ? ParticleHelper.SNOWFLAKE : ParticleTypes.SNOWFLAKE, this.getX() + dx, this.getY() + dy, this.getZ() + dz, dx, dy, dz);

        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == getOwner())
            return false;
        return super.canHitEntity(entity);
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
