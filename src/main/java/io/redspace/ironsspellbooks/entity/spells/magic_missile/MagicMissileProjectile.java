package io.redspace.ironsspellbooks.entity.spells.magic_missile;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
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

public class MagicMissileProjectile extends AbstractMagicProjectile {
    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType, levelIn);
        setOwner(shooter);
    }

    public MagicMissileProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), levelIn, shooter);
    }

    @Override
    public void impactParticles(double x, double y, double z){
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 25, 0, 0, 0, .18, true);
    }

    @Override
    public float getSpeed() {
        return 3;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        //irons_spellbooks.LOGGER.debug("MagicMissileProjectile.onHitBlock");
        kill();

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        //irons_spellbooks.LOGGER.debug("MagicMissileProjectile.onHitEntity");

        if (DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellType.MAGIC_MISSILE_SPELL.getDamageSource(this, getOwner()), SchoolType.ENDER))
            kill();

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
