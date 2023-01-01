package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
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

public class ConeOfColdProjectile extends Projectile implements ItemSupplier {
    private int age;
    private float damage;

    private final ConeOfColdPart[] subEntities;
    public final ConeOfColdPart head;
    private final ConeOfColdPart neck;
    private final ConeOfColdPart body;
    private final ConeOfColdPart tail1;
    private final ConeOfColdPart tail2;
    private final ConeOfColdPart tail3;
    private final ConeOfColdPart wing1;
    private final ConeOfColdPart wing2;

    public ConeOfColdProjectile(EntityType<? extends ConeOfColdProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);

        this.head = new ConeOfColdPart(this, "head", 1.0F, 1.0F);
        this.neck = new ConeOfColdPart(this, "neck", 3.0F, 3.0F);
        this.body = new ConeOfColdPart(this, "body", 5.0F, 3.0F);
        this.tail1 = new ConeOfColdPart(this, "tail", 2.0F, 2.0F);
        this.tail2 = new ConeOfColdPart(this, "tail", 2.0F, 2.0F);
        this.tail3 = new ConeOfColdPart(this, "tail", 2.0F, 2.0F);
        this.wing1 = new ConeOfColdPart(this, "wing", 4.0F, 2.0F);
        this.wing2 = new ConeOfColdPart(this, "wing", 4.0F, 2.0F);
        this.subEntities = new ConeOfColdPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};

        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id



    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(id + i + 1);
    }

    public ConeOfColdProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), levelIn);
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
    protected void onHit(HitResult p_37260_) {
        super.onHit(p_37260_);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

//        age++;
//        if (age > EXPIRE_TIME) {
//            discard();
//            return;
//        }

        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() == HitResult.Type.ENTITY) {
                onHitEntity((EntityHitResult) hitresult);
            }

            spawnParticles();
        }

        setPos(position().add(getDeltaMovement()));
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
