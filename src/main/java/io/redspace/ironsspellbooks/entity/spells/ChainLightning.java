package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChainLightning extends AbstractMagicProjectile {
    List<Entity> allVictims;
    List<Entity> lastVictims;
    Entity initialVictim;
    public int maxConnections = 4;
    public float range = 3f;

    public ChainLightning(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        allVictims = new ArrayList<>();
        lastVictims = new ArrayList<>();
    }

    public ChainLightning(Level level, Entity owner, Entity initialVictim) {
        this(EntityRegistry.CHAIN_LIGHTNING.get(), level);
        this.setOwner(owner);
        this.setPos(initialVictim.position());
        this.initialVictim = initialVictim;
    }

    int hits;

    @Override
    public void tick() {
        super.tick();
        int f = tickCount - 1;
        if (!this.level.isClientSide && f % 4 == 0) {
            if (f == 0 && !allVictims.contains(initialVictim)) {
                //First time zap
                doHurt(initialVictim);
            } else {
                int j = lastVictims.size();
                //cannot be enhanced for
                for (int i = 0; i < j; i++) {
                    var entity = lastVictims.get(i);
                    level.getEntities(entity, entity.getBoundingBox().inflate(range), this::canHitEntity).forEach((victim) -> {
                        if (hits < maxConnections && victim.distanceToSqr(entity) < range * range && Utils.hasLineOfSight(level, entity.getEyePosition(), victim.getEyePosition(), true))
                            doHurt(victim);
                    });
                }
                lastVictims.removeAll(allVictims);
            }
            allVictims.addAll(lastVictims);
        }
    }

    public void doHurt(Entity victim) {
        hits++;
        DamageSources.applyDamage(victim, damage, SpellType.CHAIN_LIGHTNING_SPELL.getDamageSource(this, getOwner()), SchoolType.LIGHTNING);
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 10, victim.getBbWidth() / 3, victim.getBbHeight() / 3, victim.getBbWidth() / 3, 0.1, false);

        lastVictims.add(victim);
    }

    public boolean hasAlreadyZapped(Entity entity) {
        return allVictims.contains(entity) || lastVictims.contains(entity);
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return pTarget != getOwner() && !hasAlreadyZapped(pTarget) && super.canHitEntity(pTarget);
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
