package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;

public class ExtendedEvokerFang extends EvokerFangs implements AntiMagicSusceptible {

    private final float damage;
    private boolean sentSpikeEvent;
    private int warmupDelayTicks;
    private boolean attackStarted;

    public ExtendedEvokerFang(Level pLevel, double pX, double pY, double pZ, float pYRot, int pWarmupDelay, LivingEntity pOwner, float damage) {
        super(pLevel, pX, pY, pZ, pYRot, pWarmupDelay, pOwner);
        this.warmupDelayTicks = pWarmupDelay;
        if (warmupDelayTicks < 0)
            warmupDelayTicks = 0;
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void tick() {
        baseTick();
        if (warmupDelayTicks == 0) {
            attackStarted = true;
            this.level().broadcastEntityEvent(this, (byte) 4);
        }
        if (attackStarted) {
            if (warmupDelayTicks == -8) {
                if (this.level().isClientSide) {
                    for (int i = 0; i < 12; ++i) {
                        double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double) this.getBbWidth() * 0.5D;
                        double d1 = this.getY() + 0.05D + this.random.nextDouble();
                        double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double) this.getBbWidth() * 0.5D;
                        double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                        double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    }
                } else {
                    for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.4D, 0.0D, 0.4D))) {
                        this.dealDamageTo(livingentity);
                    }
                }
            }
        }
        if (--warmupDelayTicks < -22)
            this.discard();

    }

    private void dealDamageTo(LivingEntity pTarget) {
        LivingEntity livingentity = this.getOwner();
        if (pTarget.isAlive() && !pTarget.isInvulnerable() && pTarget != livingentity) {
            var spell = SpellRegistry.FANG_STRIKE_SPELL.get();
            DamageSources.applyDamage(pTarget, damage, spell.getDamageSource(this, getOwner()));
        }
    }

    @Override
    public float getAnimationProgress(float pPartialTicks) {
        if (!this.attackStarted) {
            return 0.0F;
        } else {
            int lifeTicks = warmupDelayTicks + 22;
            int i = lifeTicks - 2;
            return i <= 0 ? 1.0F : 1.0F - ((float) i - pPartialTicks) / 20.0F;
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
