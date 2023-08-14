package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockEntity;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EarthquakeAoe extends AoeEntity implements AntiMagicSusceptible {

    public EarthquakeAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.reapplicationDelay = 30;
    }

    public EarthquakeAoe(Level level) {
        this(EntityRegistry.EARTHQUAKE_AOE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = SpellRegistry.EARTHQUAKE_SPELL.get().getDamageSource(this, getOwner());
        DamageSources.ignoreNextKnockback(target);
        target.hurt(damageSource, getDamage());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, (int) getDamage()));
    }

    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public void ambientParticles() {

    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            var level = this.level;
            var radius = this.getRadius();
            Vec3 vec3 = this.position().add(Mth.randomBetween(this.random, -radius, radius), 0, Mth.randomBetween(this.random, -radius, radius));
            BlockPos blockPos = new BlockPos(Utils.moveToRelativeGroundLevel(level, vec3, 4)).below();
            if (level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos, Direction.UP)) {
                FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(level, blockPos, level.getBlockState(blockPos));
                fallingblockentity.setPos(fallingblockentity.position().add(0, 1, 0));
                fallingblockentity.setDeltaMovement(0, .25, 0);
            }
            //IronsSpellbooks.LOGGER.debug("Earthquake ghostFallingblock: {} {}", blockPos, level.getBlockState(blockPos));
            //VisualFallingBlockEntity fallingblockentity = new VisualFallingBlockEntity(level, blockPos.getX() + 0.5D, blockPos.getY() + 0.55, blockPos.getZ() + 0.5D, level.getBlockState(blockPos));
            //level.addFreshEntity(fallingblockentity);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 3F);
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.ENTITY_EFFECT;
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }

}
