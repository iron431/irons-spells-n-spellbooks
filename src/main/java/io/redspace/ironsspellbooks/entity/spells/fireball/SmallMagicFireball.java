package io.redspace.ironsspellbooks.entity.spells.fireball;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class SmallMagicFireball extends AbstractMagicProjectile implements ItemSupplier {
    public SmallMagicFireball(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public SmallMagicFireball(Level pLevel, LivingEntity pShooter) {
        this(EntityRegistry.SMALL_FIREBALL_PROJECTILE.get(), pLevel);
        this.setOwner(pShooter);
    }

    public void shoot(Vec3 rotation, float innaccuracy) {
        Vec3 offset = Utils.getRandomVec3(1).normalize().scale(innaccuracy);
        super.shoot(rotation.add(offset));
    }

    @Nullable
    LivingEntity cachedHomingTarget;
    @Nullable
    UUID homingTargetUUID;

    public void setHomingTarget(LivingEntity entity) {
        this.homingTargetUUID = entity.getUUID();
        this.cachedHomingTarget = entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if (this.homingTargetUUID != null) {
                //handle homing
                if (cachedHomingTarget == null) {
                    cachedHomingTarget = (LivingEntity) ((ServerLevel) this.level).getEntity(homingTargetUUID);
                    if (cachedHomingTarget == null) {
                        homingTargetUUID = null;
                        return;
                    }
                }
                var motion = this.getDeltaMovement();
                var speed = this.getDeltaMovement().length();
                var delta = cachedHomingTarget.getBoundingBox().getCenter().subtract(this.position());
                float f = .08f;
                var newMotion = new Vec3(Mth.lerp(f, motion.x, delta.x), Mth.lerp(f, motion.y, delta.y), Mth.lerp(f, motion.z, delta.z)).normalize().scale(speed);
                this.setDeltaMovement(newMotion);
                if (this.tickCount > 10 && newMotion.dot(delta) < 0) {
                    // after a decent bit into our flight, if we are past our target, lose tracking
                    homingTargetUUID = null;
                }
            }
        }
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        for (int i = 0; i < 2; i++) {
            Vec3 random = Utils.getRandomVec3(.1);
            this.level.addParticle(ParticleHelper.EMBERS, d0 - random.x, d1 + 0.5D - random.y, d2 - random.z, random.x * .5f, random.y * .5f, random.z * .5f);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 1.85f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }


    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (!this.level.isClientSide) {
            var target = pResult.getEntity();
            var owner = getOwner();
            DamageSources.applyDamage(target, damage, SpellRegistry.BLAZE_STORM_SPELL.get().getDamageSource(this, owner));
            if (target.getUUID().equals(homingTargetUUID)) {
                target.invulnerableTime = 0;
            }
        }
    }

    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if (!this.level.isClientSide) {
            if (ServerConfigs.SPELL_GREIFING.get()) {
                BlockPos blockpos = pResult.getBlockPos().relative(pResult.getDirection());
                if (this.level.isEmptyBlock(blockpos)) {
                    this.level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level, blockpos));
                }
            }
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level.isClientSide) {
            this.discard();
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homingTargetUUID != null) {
            tag.putUUID("homingTarget", homingTargetUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("homingTarget", 11)) {
            this.homingTargetUUID = tag.getUUID("homingTarget");
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }
}
