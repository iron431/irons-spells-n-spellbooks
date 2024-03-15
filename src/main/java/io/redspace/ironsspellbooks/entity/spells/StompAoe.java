package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class StompAoe extends AbstractMagicProjectile {

    int step;
    int maxSteps;

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    public StompAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.maxSteps = 5;
    }

    public StompAoe(Level level, int steps, float yRot) {
        this(EntityRegistry.STOMP_AOE.get(), level);
        this.maxSteps = steps;
        this.setYRot(yRot);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if (tickCount % 1 == 0) {
                checkHits();
            }
            if (step > maxSteps) {
                discard();
            }
        }
    }

    protected void checkHits() {
        if (!level.isClientSide) {
            step++;
            int width = Math.max(step / 2, 2);
            float angle = (this.getYRot()) * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(Mth.sin(-angle), 0, Mth.cos(-angle));
            Vec3 orth = new Vec3(-forward.z, 0, forward.x);

            Vec3 center = this.position().add(forward.scale(step));
            Vec3 leftBound = Utils.moveToRelativeGroundLevel(level, center.subtract(orth.scale(width)), 2).add(0, 0.75, 0);
            Vec3 rightBound = Utils.moveToRelativeGroundLevel(level, center.add(orth.scale(width)), 2).add(0, 0.75, 0);

            //MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, center.x, center.y, center.z, 30, 0, 1, 0, 0, true);
            //MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, leftBound.x, leftBound.y, leftBound.z, 30, 0, 1, 0, 0, true);
            //MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, rightBound.x, rightBound.y, rightBound.z, 30, 0, 1, 0, 0, true);
            //for (int i = 0; i < 30; i++) {
            //    Vec3 pos = leftBound.add(rightBound.subtract(leftBound).scale(i / 30f));
            //    MagicManager.spawnParticles(level, ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            //}
            level.getEntities(this, new AABB(leftBound.add(0, -1, 0), rightBound.add(0, 1, 0))).forEach((entity) -> {
                if (canHitEntity(entity) && Utils.checkEntityIntersecting(entity, leftBound, rightBound, .5f).getType() != HitResult.Type.MISS) {
                    if (DamageSources.applyDamage(entity, getDamage(), SpellRegistry.STOMP_SPELL.get().getDamageSource(this, getOwner()))) {
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.knockback(this.explosionRadius * -.35f, forward.x, forward.z);
                        }
                    }
                }
            });
            for (int i = 0; i < step; i++) {
                Vec3 pos = leftBound.add(rightBound.subtract(leftBound).scale(i / (float) step));
                var blockPos = BlockPos.containing(Utils.moveToRelativeGroundLevel(level, pos, 2)).below();
                float impulseStrength = Utils.random.nextFloat() * .15f + 0.2f;
                Utils.createTremorBlock(level, blockPos, impulseStrength);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("stompStep", step);
        pCompound.putInt("maxSteps", maxSteps);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.step = pCompound.getInt("stompStep");
        this.maxSteps = pCompound.getInt("maxSteps");
    }
}