package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ArrowVolleyEntity extends AbstractMagicProjectile {
    public ArrowVolleyEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    int rows;
    int arrowsPerRow;
    int delay = 5;

    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (tickCount % delay == 0) {
                //do volley
                int arrows = arrowsPerRow;
                float speed = .85f;
                Vec3 motion = Vec3.directionFromRotation(getXRot() - tickCount / 5f * 7, this.getYRot()).normalize().scale(speed);
                Vec3 orth = new Vec3(-Mth.cos(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI), 0, Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI));
                for (int i = 0; i < arrows; i++) {
                    float distance = (i - arrows * .5f) * .7f;
                    SmallMagicArrow arrow = new SmallMagicArrow(this.level, this.getOwner());
                    arrow.setDamage(this.getDamage());
                    var spawn = this.position().add(orth.scale(distance));
                    arrow.setPos(spawn);
                    arrow.shoot(motion.add(Utils.getRandomVec3(.04f)));
                    arrow.setOwner(this.getOwner());
                    level.addFreshEntity(arrow);
                    MagicManager.spawnParticles(level, ParticleTypes.FIREWORK, spawn.x, spawn.y, spawn.z, 2, .1, .1, .1, .05, false);
                }
                level.playSound(null, position().x, position().y, position().z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.0f, 1.1f + Utils.random.nextFloat() * .3f);
                level.playSound(null, position().x, position().y, position().z, SoundRegistry.BOW_SHOOT.get(), SoundSource.NEUTRAL, 2, Utils.random.nextIntBetweenInclusive(16, 20) * .1f);
            } else if (tickCount > rows * delay) {
                discard();
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("rows", rows);
        tag.putInt("arrowsPerRow", arrowsPerRow);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rows = tag.getInt("rows");
        this.arrowsPerRow = tag.getInt("arrowsPerRow");
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setArrowsPerRow(int arrowsPerRow) {
        this.arrowsPerRow = arrowsPerRow;
    }


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
}
