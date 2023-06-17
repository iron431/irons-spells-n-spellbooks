package io.redspace.ironsspellbooks.entity.spells.guiding_bolt;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public class GuidingBoltProjectile extends AbstractMagicProjectile {
    public GuidingBoltProjectile(EntityType<? extends GuidingBoltProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public GuidingBoltProjectile(EntityType<? extends GuidingBoltProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType, levelIn);
        setOwner(shooter);
    }

    public GuidingBoltProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.GUIDING_BOLT.get(), levelIn, shooter);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 25, 0, 0, 0, .18, true);
    }

    @Override
    public float getSpeed() {
        return 1.25f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        //irons_spellbooks.LOGGER.debug("MagicMissileProjectile.onHitBlock");
        discard();

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        //irons_spellbooks.LOGGER.debug("MagicMissileProjectile.onHitEntity");

        if (DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellType.GUIDING_BOLT_SPELL.getDamageSource(this, getOwner()), SchoolType.HOLY)) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity)
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.GUIDING_BOLT.get(), 15 * 20));
            discard();
        }

    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 2; i++) {
            double speed = .02;
            double dx = level.random.nextDouble() * 2 * speed - speed;
            double dy = level.random.nextDouble() * 2 * speed - speed;
            double dz = level.random.nextDouble() * 2 * speed - speed;
            level.addParticle(ParticleHelper.UNSTABLE_ENDER, this.getX() + dx, this.getY() + dy, this.getZ() + dz, dx, dy, dz);
            if (age > 1)
                level.addParticle(ParticleHelper.UNSTABLE_ENDER, this.getX() + dx - getDeltaMovement().x / 2, this.getY() + dy - getDeltaMovement().y / 2, this.getZ() + dz - getDeltaMovement().z / 2, dx, dy, dz);

        }
    }
}
