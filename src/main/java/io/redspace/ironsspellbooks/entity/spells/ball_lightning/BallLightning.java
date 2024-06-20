package io.redspace.ironsspellbooks.entity.spells.ball_lightning;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BallLightning extends AbstractMagicProjectile {
    int bounces;
    HashMap<UUID, Integer> victims;

    public BallLightning(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.victims = new HashMap<>();
        this.setNoGravity(true);
    }

    public BallLightning(Level level, LivingEntity shooter) {
        this(EntityRegistry.BALL_LIGHTNING.get(), level);
        setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        Vec3 pos = this.getBoundingBox().getCenter().add(getDeltaMovement());
        Vec3 random = Utils.getRandomVec3(1f).add(pos);
        pos = pos.add(getDeltaMovement());
        level.addParticle(new ZapParticleOption(random), pos.x, pos.y, pos.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, x, y, z, 12, .08, .08, .08, 0.3, false);
    }

    @Override
    public float getSpeed() {
        return 0.6f;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && canHitVictim(pTarget);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && tickCount > 80) {
            discard();
            impactParticles(getX(), this.getBoundingBox().getCenter().y, getZ());
        }
    }

    @Override
    public void handleHitDetection() {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 pos = this.position();
        Vec3 vec32 = pos.add(vec3);
        HitResult hitresult = level.clip(new ClipContext(pos, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            //block hits
            onHit(hitresult);
        } else {
            //entity hits
            var entities = level.getEntities(this, this.getBoundingBox().inflate(0.25f), this::canHitEntity);
            for (Entity entity : entities) {
                onHit(new EntityHitResult(entity, this.getBoundingBox().getCenter().add(entity.getBoundingBox().getCenter()).scale(0.5f)));
            }
        }
    }

    public boolean canHitVictim(Entity entity) {
        var timestamp = victims.get(entity.getUUID());
        return timestamp == null || entity.tickCount - timestamp >= 10;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        var target = pResult.getEntity();
        if (target instanceof LivingEntity livingEntity) {
            DamageSources.ignoreNextKnockback(livingEntity);
        }
        DamageSources.applyDamage(target, getDamage(), SpellRegistry.BALL_LIGHTNING_SPELL.get().getDamageSource(this, getOwner()));
        victims.put(target.getUUID(), target.tickCount);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        switch (pResult.getDirection()) {
            case UP, DOWN ->
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, this.isNoGravity() ? -1 : -.8f, 1));
            case EAST, WEST -> this.setDeltaMovement(this.getDeltaMovement().multiply(-1, 1, 1));
            case NORTH, SOUTH -> this.setDeltaMovement(this.getDeltaMovement().multiply(1, 1, -1));
        }
        if (++bounces >= 6) {
            discard();
        }
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundRegistry.CHAIN_LIGHTNING_CHAIN.get());
    }

}
