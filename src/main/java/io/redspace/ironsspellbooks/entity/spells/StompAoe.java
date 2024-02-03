package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.nature.StompSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
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
            if (tickCount % 3 == 0) {
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
            int width = step / 2;
            float angle = (this.getYRot()) * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(Mth.sin(-angle), 0, Mth.cos(-angle));
            Vec3 orth = new Vec3(-forward.z, 0, forward.x);

            Vec3 center = this.position().add(forward.scale(step + 1));
            Vec3 leftBound = center.subtract(orth.scale(width));
            Vec3 rightBound = center.add(orth.scale(width));

            //MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, center.x, center.y, center.z, 30, 0, 1, 0, 0, true);
            //MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, leftBound.x, leftBound.y, leftBound.z, 30, 0, 1, 0, 0, true);
            //MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, rightBound.x, rightBound.y, rightBound.z, 30, 0, 1, 0, 0, true);
            for (int i = 0; i < 30; i++) {
                Vec3 pos = leftBound.add(rightBound.subtract(leftBound).scale(i / 30f));
                MagicManager.spawnParticles(level, ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            }
            level.getEntities(this, new AABB(leftBound, rightBound)).forEach((entity) -> {
                if (canHitEntity(entity) && Utils.checkEntityIntersecting(entity, leftBound, rightBound, .1f).getType() != HitResult.Type.MISS) {
                    //todo: real damage
                    entity.hurt(SpellRegistry.STOMP_SPELL.get().getDamageSource(this, getOwner()), 10);
                }
            });
            //todo: visual block entities
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("stompStep", step);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.step = pCompound.getInt("stompStep");
    }
}