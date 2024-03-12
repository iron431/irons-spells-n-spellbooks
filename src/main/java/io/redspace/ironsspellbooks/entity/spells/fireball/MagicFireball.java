package io.redspace.ironsspellbooks.entity.spells.fireball;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.network.spell.ClientboundFieryExplosionParticles;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MagicFireball extends AbstractMagicProjectile implements ItemSupplier {
    public MagicFireball(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MagicFireball(Level pLevel, LivingEntity pShooter) {
        this(EntityRegistry.MAGIC_FIREBALL.get(), pLevel);
        this.setOwner(pShooter);
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        for (int i = 0; i < 8; i++) {
            Vec3 motion = Utils.getRandomVec3(.1).subtract(getDeltaMovement().scale(.1f));
            Vec3 pos = Utils.getRandomVec3(.3);
            this.level.addParticle(ParticleHelper.EMBERS, d0 + pos.x, d1 + 0.5D + pos.y, d2 + pos.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 1.15f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.GENERIC_EXPLODE);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level.isClientSide) {
            impactParticles(xOld, yOld, zOld);
            float explosionRadius = getExplosionRadius();
            var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.distanceToSqr(hitResult.getLocation());
                if (distance < explosionRadius * explosionRadius && canHitEntity(entity)) {
                    double p = (1 - Math.pow(Math.sqrt(distance) / (explosionRadius), 3));
                    float damage = (float) (this.damage * p);
                    DamageSources.applyDamage(entity, damage, SpellRegistry.FIREBALL_SPELL.get().getDamageSource(this, getOwner()));
                }
            }
            if (ServerConfigs.SPELL_GREIFING.get()) {
                Explosion explosion = new Explosion(level, null, SpellRegistry.FIREBALL_SPELL.get().getDamageSource(this, getOwner()), null, this.getX(), this.getY(), this.getZ(), this.getExplosionRadius(), true, Explosion.BlockInteraction.DESTROY);
                if (!net.minecraftforge.event.ForgeEventFactory.onExplosionStart(level, explosion)) {
                    explosion.explode();
                    explosion.finalizeExplosion(false);
                }
            }
            Messages.sendToPlayersTrackingEntity(new ClientboundFieryExplosionParticles(new Vec3(getX(), getY() + .15f, getZ()), getExplosionRadius()), this);
            playSound(SoundEvents.GENERIC_EXPLODE, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && Utils.hasLineOfSight(level, this.getBoundingBox().getCenter(), pTarget.getBoundingBox().getCenter(), true);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }
}
