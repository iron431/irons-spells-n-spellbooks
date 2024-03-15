package io.redspace.ironsspellbooks.entity.spells.lightning_lance;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LightningLanceProjectile extends AbstractMagicProjectile {

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement());
        level.addParticle(ParticleHelper.ELECTRICITY, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, x, y, z, 75, .1, .1, .1, 2, true);
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, x, y, z, 75, .1, .1, .1, .5, false);
    }

    @Override
    public float getSpeed() {
        return 3f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    public LightningLanceProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(false);
    }

    public LightningLanceProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.LIGHTNING_LANCE_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellRegistry.LIGHTNING_LANCE_SPELL.get().getDamageSource(this, getOwner()));

    }

    @Override
    protected void onHit(HitResult pResult) {
        //irons_spellbooks.LOGGER.debug("Boom");

        if (!level.isClientSide) {
            this.playSound(SoundEvents.TRIDENT_THUNDER, 6, .65f);
//            irons_spellbooks.LOGGER.debug("{}",pos);
//            //Beam
//            for (int i = 0; i < 40; i++) {
//                Vec3 randomVec = new Vec3(
//                        Utils.random.nextDouble() * .25 - .125,
//                        Utils.random.nextDouble() * .25 - .125,
//                        Utils.random.nextDouble() * .25 - .125
//                );
//                //level.addParticle(ParticleHelper.ELECTRICITY, pos.x + randomVec.x, pos.y + randomVec.y + i * .25, pos.z + randomVec.z, randomVec.x * .2, randomVec.y * .2, randomVec.z * .2);
//                level.addParticle(ParticleHelper.ELECTRICITY, pos.x, pos.y, pos.z, 0,0,0);
//            }
        }
        super.onHit(pResult);
        this.discard();
    }

    public int getAge(){
        return tickCount;
    }
}
