package io.redspace.ironsspellbooks.entity.spells.sunbeam;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SunbeamEntity extends AoeEntity implements AntiMagicSusceptible {

    public SunbeamEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setRadius((float) (this.getBoundingBox().getXsize() * .5f));
        this.setNoGravity(true);
    }

    public SunbeamEntity(Level level) {
        this(EntityRegistry.SUNBEAM.get(), level);
    }


    public static final int WARMUP_TIME = 20;

    @Override
    public void tick() {
        if (tickCount == WARMUP_TIME) {
            checkHits();
            if (!level.isClientSide) {
                //MagicManager.spawnParticles(level, ParticleTypes.FIREWORK, getX(), getY(), getZ(), 9, getRadius() * .7f, .2f, getRadius() * .7f, 1, true);
                MagicManager.spawnParticles(level, ParticleHelper.EMBERS, getX(), getY() + 0.06, getZ(), 50, getRadius() * .7f, .2f, getRadius() * .7f, 0.6f, true);
                MagicManager.spawnParticles(level, new BlastwaveParticleOptions(1f, 0.85f, 0.4f, 7f), getX(), getY() + 0.06, getZ(), 1, 0, 0, 0, 0, true);
            }
        }

        if (this.tickCount > WARMUP_TIME) {
            discard();
        }
    }


    @Override
    public void applyEffect(LivingEntity target) {
        DamageSources.applyDamage(target, getDamage(), SpellRegistry.SUNBEAM_SPELL.get().getDamageSource(this, getOwner()));
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(1, 1, 1);
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

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }
}
