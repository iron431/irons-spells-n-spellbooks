package io.redspace.ironsspellbooks.entity.spells.icicle;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public class IcicleProjectile extends AbstractMagicProjectile {

    public IcicleProjectile(EntityType<? extends IcicleProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public IcicleProjectile(Level levelIn, LivingEntity shooter) {
        super(EntityRegistry.ICICLE_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        kill();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        DamageSources.applyDamage(entityHitResult.getEntity(), getDamage(), SpellRegistry.ICICLE_SPELL.get().getDamageSource(this, getOwner()));
    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 1; i++) {
            double speed = .05;
            double dx = Utils.random.nextDouble() * 2 * speed - speed;
            double dy = Utils.random.nextDouble() * 2 * speed - speed;
            double dz = Utils.random.nextDouble() * 2 * speed - speed;
            level.addParticle(Utils.random.nextDouble() < .3 ? ParticleHelper.SNOWFLAKE : ParticleTypes.SNOWFLAKE, this.getX() + dx, this.getY() + dy, this.getZ() + dz, dx, dy, dz);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, x, y, z, 15, .1, .1, .1, .1, true);
    }

    @Override
    public float getSpeed() {
        return 1.4f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundRegistry.ICE_IMPACT.get());
    }
}
