package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FangSwirlEntity extends AoeEntity {

    public Vec3 getStartPos() {
        return startPos;
    }

    public void setStartPos(Vec3 startPos) {
        this.startPos = startPos;
    }

    protected Vec3 startPos;

    public FangSwirlEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.reapplicationDelay = 6;
    }

    @Override
    public void applyEffect(LivingEntity target) {

    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
        // swirling line towards destination during delay
        if (startPos != null && !level.isClientSide && tickCount < getDelay()) {
            float f = tickCount / (float) getDelay();
            Vec3 forward = position().subtract(startPos).multiply(1, 0, 1).normalize();
            Vec3 right = new Vec3(-forward.z, 0, forward.x);
            Vec3 spawn = startPos.lerp(position(), f);
            float phase = tickCount * 0.5f;
            float threshold = 3;
            float distance = (float) position().subtract(spawn).horizontalDistance();
            float strength = distance > threshold ? 1 : distance / threshold;
            Vec3 oscillation = right.scale(Mth.sin(phase) * 2 * strength);
            spawn = Utils.moveToRelativeGroundLevel(level, spawn.add(oscillation), 6);
            float yrot = Utils.getAngle(startPos.x, startPos.z, getX(), getZ());
            if (!level.getBlockState(BlockPos.containing(spawn).below()).isAir()) {
                ExtendedEvokerFang fang = new ExtendedEvokerFang(level, spawn.x, spawn.y, spawn.z, yrot, 0, this.getOwner(), this.getDamage());
                level.addFreshEntity(fang);
            }
        }
    }

    @Override
    protected void checkHits() {
        if (level.isClientSide) {
            return;
        }
        float radius = getRadius();
        if (radius < 0.05f) {
            return;
        }
        float angle = (tickCount - getDelay()) * Mth.TWO_PI * 0.01f;
        float yawDeg = angle * Mth.RAD_TO_DEG;
        Vec3 axis = Vec3.directionFromRotation(0, yawDeg);
        float fangYawRad = (yawDeg - 90f) * Mth.DEG_TO_RAD;
        Vec3 center = position();
        float density = 0.85f;
        int count = Mth.ceil((2 * radius / density)) + 1;
        for (int i = 0; i < count; i++) {
            double t = Mth.lerp(i / (count - 1f), -1.0, 1.0);
            Vec3 spawn = center.add(axis.scale(t * radius));
            spawn = Utils.moveToRelativeGroundLevel(level, spawn, 4);
            int delay = Math.abs((int) (t * radius * 0.5));
            ExtendedEvokerFang fang = new ExtendedEvokerFang(level, spawn.x, spawn.y, spawn.z, fangYawRad, delay, getOwner(), getDamage());
            level.addFreshEntity(fang);
        }
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("startPos", Tag.TAG_COMPOUND)) {
            this.startPos = NBT.readVec3(compound.getCompound("startPos"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("startPos", NBT.writeVec3Pos(startPos));
    }
}
