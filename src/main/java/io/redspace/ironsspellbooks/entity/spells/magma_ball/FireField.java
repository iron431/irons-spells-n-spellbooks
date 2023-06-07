package io.redspace.ironsspellbooks.entity.spells.magma_ball;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class FireField extends AoeEntity {

    //TODO: 1.19.4 port: add to fire damage tag
    public static final DamageSource DAMAGE_SOURCE = new DamageSource(Holder.direct(new DamageType("fire_field", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0f)));

    public FireField(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public FireField(Level level) {
        this(EntityRegistry.FIRE_FIELD.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = DamageSources.indirectDamageSource(DAMAGE_SOURCE, this, getOwner());
        target.hurt(damageSource, getDamage());
        target.setSecondsOnFire(3);
    }

    @Override
    public float getParticleCount() {
        return 0.7f * getRadius();
    }

    @Override
    protected float particleYOffset() {
        return .25f;
    }

    @Override
    protected float getParticleSpeedModifier() {
        return 1.4f;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleHelper.FIRE;
    }
}
