package io.redspace.ironsspellbooks.entity.spells.poison_cloud;

import io.redspace.ironsspellbooks.entity.spells.AOEProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class PoisonCloudEntity extends AOEProjectile {

    public PoisonCloudEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public PoisonCloudEntity(Level level) {
        this(EntityRegistry.POISON_CLOUD.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        //DamageSources.applyDamage(target,getDamage(),)
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
    }

    @Override
    public float getParticleCount() {
        return .15f;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleHelper.POISON_CLOUD;
    }
}
