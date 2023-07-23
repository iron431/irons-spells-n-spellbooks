package io.redspace.ironsspellbooks.entity.spells.icicle;

import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

//https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
//https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
//https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
//https://github.com/maximumpower55/Aura

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
        boolean hit = DamageSources.applyDamage(entityHitResult.getEntity(), getDamage(), SpellRegistry.ICICLE_SPELL.get().getDamageSource(this, getOwner()), SchoolType.ICE);
 //Ironsspellbooks.logger.debug("IcilePorjectile: Hit: {}",hit);
        if (hit && entityHitResult.getEntity() instanceof LivingEntity target && !level.isClientSide && target.canFreeze()) {
            target.setTicksFrozen(target.getTicksFrozen() + 165);
        }

    }

    @Override
    public void trailParticles() {

        for (int i = 0; i < 1; i++) {
            double speed = .05;
            double dx = level.random.nextDouble() * 2 * speed - speed;
            double dy = level.random.nextDouble() * 2 * speed - speed;
            double dz = level.random.nextDouble() * 2 * speed - speed;
            level.addParticle(level.random.nextDouble() < .3 ? ParticleHelper.SNOWFLAKE : ParticleTypes.SNOWFLAKE, this.getX() + dx, this.getY() + dy, this.getZ() + dz, dx, dy, dz);

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

    @Override
    public boolean respectsGravity() {
        return true;
    }
}
