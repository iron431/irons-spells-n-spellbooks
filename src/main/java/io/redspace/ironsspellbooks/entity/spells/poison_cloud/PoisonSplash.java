package io.redspace.ironsspellbooks.entity.spells.poison_cloud;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PoisonSplash extends AoeEntity {

    public PoisonSplash(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setRadius((float) (this.getBoundingBox().getXsize() * .5f));
    }

    public PoisonSplash(Level level) {
        this(EntityRegistry.POISON_SPLASH.get(), level);
    }

    boolean playedParticles;

    @Override
    public void tick() {
        if (!playedParticles) {
            playedParticles = true;
            if (level().isClientSide) {
                for (int i = 0; i < 150; i++) {
                    Vec3 pos = new Vec3(Utils.getRandomScaled(.5f), Utils.getRandomScaled(.2f), this.random.nextFloat() * getRadius()).yRot(this.random.nextFloat() * 360);
                    Vec3 motion = new Vec3(
                            Utils.getRandomScaled(.06f),
                            this.random.nextDouble() * -.8 - .5,
                            Utils.getRandomScaled(.06f)
                    );

                    level().addParticle(ParticleHelper.ACID, getX() + pos.x, getY() + pos.y + getBoundingBox().getYsize(), getZ() + pos.z, motion.x, motion.y, motion.z);
                }
            }else{
                MagicManager.spawnParticles(level(), ParticleHelper.POISON_CLOUD, getX(), getY()  + getBoundingBox().getYsize(), getZ(), 9, getRadius() * .7f, .2f, getRadius() * .7f, 1, true);

            }
        }

        if (tickCount == 4) {
            checkHits();
            if (!level().isClientSide)
                MagicManager.spawnParticles(level(), ParticleHelper.POISON_CLOUD, getX(), getY(), getZ(), 9, getRadius() * .7f, .2f, getRadius() * .7f, 1, true);
            createPoisonCloud();
        }

        if (this.tickCount > 6) {
            discard();
        }
    }

    public void createPoisonCloud() {
        if (!level().isClientSide) {
            PoisonCloud cloud = new PoisonCloud(level());
            cloud.setOwner(getOwner());
            cloud.setDuration(getEffectDuration());
            cloud.setDamage(getDamage() * .1f);
            cloud.moveTo(this.position());
            level().addFreshEntity(cloud);
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (DamageSources.applyDamage(target, getDamage(), SpellRegistry.POISON_SPLASH_SPELL.get().getDamageSource(this, getOwner())))
            target.addEffect(new MobEffectInstance(MobEffects.POISON, getEffectDuration(), 0));
    }


    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public void refreshDimensions() {
        return;
    }

    @Override
    public void ambientParticles() {
        return;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }
}
