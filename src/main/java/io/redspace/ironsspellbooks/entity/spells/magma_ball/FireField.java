package io.redspace.ironsspellbooks.entity.spells.magma_ball;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FireField extends AoeEntity {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource(String.format("%s.%s", IronsSpellbooks.MODID, "fire_field"));

    public FireField(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public FireField(Level level) {
        this(EntityRegistry.FIRE_FIELD.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = DamageSources.indirectDamageSource(DAMAGE_SOURCE, this, getOwner());
        DamageSources.ignoreNextKnockback(target);
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
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ParticleHelper.FIRE);
    }
}
