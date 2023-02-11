package com.example.testmod.entity.lightning_lance;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.damage.DamageSources;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.util.ParticleHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.example.testmod.damage.DamageSources.LIGHTNING_LANCE_DAMAGE;

public class LightningLanceProjectile extends Projectile {
    private static final int EXPIRE_TIME = 20 * 20;
    public int age;

    public LightningLanceProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LightningLanceProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.LIGHTNING_LANCE_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(2.5));
    }

    @Override
    protected void defineSynchedData() {

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

        age++;
        Vec3 vec3 = this.getDeltaMovement();

        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }
    }

    private void spawnParticles() {

    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        DamageSources.applyDamage(entityHitResult.getEntity(), 100, LIGHTNING_LANCE_DAMAGE, SchoolType.LIGHTNING, getOwner());
    }

    @Override
    protected void onHit(HitResult pResult) {
        //TestMod.LOGGER.debug("Boom");

        if (!level.isClientSide) {
            Vec3 pos = pResult.getLocation();
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, pos.x, pos.y, pos.z, 75, .1, .1, .1, 2, true);
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, pos.x, pos.y, pos.z, 75, .1, .1, .1, .5, false);
            this.playSound(SoundEvents.TRIDENT_THUNDER, 4, .65f);
//            TestMod.LOGGER.debug("{}",pos);
//            //Beam
//            for (int i = 0; i < 40; i++) {
//                Vec3 randomVec = new Vec3(
//                        level.random.nextDouble() * .25 - .125,
//                        level.random.nextDouble() * .25 - .125,
//                        level.random.nextDouble() * .25 - .125
//                );
//                //level.addParticle(ParticleHelper.ELECTRICITY, pos.x + randomVec.x, pos.y + randomVec.y + i * .25, pos.z + randomVec.z, randomVec.x * .2, randomVec.y * .2, randomVec.z * .2);
//                level.addParticle(ParticleHelper.ELECTRICITY, pos.x, pos.y, pos.z, 0,0,0);
//            }
        }
        super.onHit(pResult);

        this.discard();
    }
}
