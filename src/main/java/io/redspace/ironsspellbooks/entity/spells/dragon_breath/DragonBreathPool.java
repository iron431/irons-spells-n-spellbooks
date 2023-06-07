package io.redspace.ironsspellbooks.entity.spells.dragon_breath;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class DragonBreathPool extends AoeEntity {

    //TODO: 1.19.4 port: add to magic damage tag
    public static final DamageSource DAMAGE_SOURCE = new DamageSource(Holder.direct(new DamageType(SpellType.DRAGON_BREATH_SPELL.getId() + "_pool", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0f)));

    public DragonBreathPool(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setCircular();
        setRadius(1.8f);
        this.radiusOnUse = -.15f;
        this.radiusPerTick = -.02f;
        IronsSpellbooks.LOGGER.debug("Creating DragonBreathPool");
    }

    public DragonBreathPool(Level level) {
        this(EntityRegistry.DRAGON_BREATH_POOL.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = DamageSources.indirectDamageSource(DAMAGE_SOURCE, this, getOwner());

        target.hurt(damageSource, getDamage());
    }

    @Override
    public float getParticleCount() {
        return 4f;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }
}
