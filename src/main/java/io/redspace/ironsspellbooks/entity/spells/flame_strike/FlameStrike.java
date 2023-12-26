package io.redspace.ironsspellbooks.entity.spells.flame_strike;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

//TODO: well, this really could've been a particle. However, it already works.
public class FlameStrike extends AoeEntity {
    public FlameStrike(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    LivingEntity target;

    public FlameStrike(Level level) {
        this(EntityRegistry.FLAME_STRIKE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    public final int ticksPerFrame = 2;
    public final int deathTime = ticksPerFrame * 4;

    @Override
    public void tick() {
        if (!firstTick) {
            checkHits();
            firstTick = true;
        }
        if (tickCount >= deathTime)
            discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
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
    public float getParticleCount() {
        return 0;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleHelper.FIRE;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
