package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LightningStrike extends AoeEntity {
    public LightningStrike(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setRadius(3);
        setCircular();
    }

    public LightningStrike(Level level) {
        this(EntityRegistry.LIGHTNING_STRIKE.get(), level);
    }

    static final int chargeTime = 20;
    static final int vfxHeight = 15;
    @Override
    public void tick() {
        if (level.isClientSide) {
            return;
        }
        if (tickCount == 1) {
            int total = 5;
            int light = Utils.random.nextInt(total);
            Vec3 location = this.position().add(0, vfxHeight, 0);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_LIGHT, location.x, location.y, location.z, light, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_DARK, location.x, location.y, location.z, total - light, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, new ShockwaveParticleOptions(SchoolRegistry.LIGHTNING.get().getTargetingColor(), chargeTime * -1.5f * .05f, true), getX(), getY(), getZ(), 1, 0, 0, 0, 0, true);
        }
        if (tickCount == chargeTime) {
            checkHits();
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, getX(), getY(), getZ(), 25, .2f, .2f, .2f, .25, true);
            MagicManager.spawnParticles(level, ParticleHelper.FIERY_SPARKS, getX(), getY(), getZ(), 5, .2f, .2f, .2f, .125, true);
            Vec3 bottom = this.position();
            Vec3 top = bottom.add(0, vfxHeight, 0);
            Vec3 middle = bottom.add(Utils.getRandomScaled(2), Utils.random.nextIntBetweenInclusive(3, vfxHeight - 3), Utils.getRandomScaled(2));
            MagicManager.spawnParticles(level, new ZapParticleOption(top), middle.x, middle.y, middle.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new ZapParticleOption(middle), getX(), getY(), getZ(), 1, 0, 0, 0, 0, true);
            if (Utils.random.nextFloat() < .3f) {
                Vec3 split = middle.add(Utils.getRandomScaled(2), -Math.abs(Utils.getRandomScaled(2)), Utils.getRandomScaled(2));
                MagicManager.spawnParticles(level, new ZapParticleOption(middle), split.x, split.y, split.z, 1, 0, 0, 0, 0, true);
            }
            playSound(SoundRegistry.SMALL_LIGHTNING_STRIKE.get(), 2f, .8f + random.nextFloat() * .5f);
        }
        if (this.tickCount > chargeTime) {
            discard();
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
        DamageSources.applyDamage(target, getDamage(), SpellRegistry.THUNDERSTORM_SPELL.get().getDamageSource(this, getOwner()));
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }
}
