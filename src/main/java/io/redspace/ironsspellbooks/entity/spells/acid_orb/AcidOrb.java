package io.redspace.ironsspellbooks.entity.spells.acid_orb;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AcidOrb extends AbstractMagicProjectile {
    public AcidOrb(EntityType<? extends AcidOrb> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public AcidOrb(Level level, Entity shooter) {
        this(EntityRegistry.ACID_ORB.get(), level);
        setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement().scale(2));
        level.addParticle(ParticleHelper.ACID, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.ACID, x, y, z, 55, .08, .08, .08, 0.3, true);
        MagicManager.spawnParticles(level, ParticleHelper.ACID_BUBBLE, x, y, z, 25, .08, .08, .08, 0.3, false);
    }

    @Override
    protected float getBaseSpeed() {
        return 1;
    }

    @Override
    protected void onHit(@NotNull HitResult hitresult) {
        super.onHit(hitresult);
        if (!this.level.isClientSide) {
            float explosionRadius = 3.5f;
            var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.position().distanceTo(hitresult.getLocation());
                if (distance < explosionRadius && Utils.hasLineOfSight(level, hitresult.getLocation(), entity.getEyePosition(), true)) {
                    if (entity instanceof LivingEntity livingEntity && livingEntity != getOwner())
                        livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.REND, getEffectDuration(), getEffectAmplifier()));
                }
            }
            this.discardHelper(hitresult);
        }
    }

    @Override
    public Optional<PlayableSound> getImpactSound() {
        return impactSound(SoundRegistry.ACID_ORB_IMPACT);
    }

}
