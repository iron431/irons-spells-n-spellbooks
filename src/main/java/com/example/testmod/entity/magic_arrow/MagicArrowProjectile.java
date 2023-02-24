package com.example.testmod.entity.magic_arrow;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.damage.DamageSources;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MagicArrowProjectile extends Projectile {
    private static final int EXPIRE_TIME = 5 * 20;
    public int age;
    private float damage;
    private final List<Entity> victims = new ArrayList<>();
    private int penetration;

    public MagicArrowProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MagicArrowProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_ARROW_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(2.3));
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void tick() {
        super.tick();
        if (age > EXPIRE_TIME || penetration >= 5) {
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

//        if (!this.isNoGravity()) {
//            Vec3 vec34 = this.getDeltaMovement();
//            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
//        }
    }

    private void spawnParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement());
        level.addParticle(ParticleHelper.UNSTABLE_ENDER, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!victims.contains(entity)) {
            DamageSources.applyDamage(entity, damage, SpellType.MAGIC_ARROW_SPELL.getDamageSource(this, getOwner()), SchoolType.ENDER);
            victims.add(entity);
        }
    }


    @Override
    protected void onHit(HitResult result) {
        TestMod.LOGGER.debug("onHit ({})", result.getType());

        penetration++;
        if (!level.isClientSide) {
            Vec3 pos = result.getLocation();
            MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, pos.x, pos.y, pos.z, 15, .1, .1, .1, .5, false);

            if (result.getType() == HitResult.Type.ENTITY) {
                level.playSound(null, new BlockPos(position()), SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2, .65f);
                TestMod.LOGGER.debug("Playing Sound");

            }

        }
//        if (result.getType() == HitResult.Type.ENTITY)
//            this.playSound(SoundRegistry.FORCE_IMPACT.get(), 2, .65f);

        super.onHit(result);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}
