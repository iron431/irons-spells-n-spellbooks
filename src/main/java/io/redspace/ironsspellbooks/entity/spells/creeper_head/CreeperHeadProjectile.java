package io.redspace.ironsspellbooks.entity.spells.creeper_head;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.evocation.ChainCreeperSpell;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class CreeperHeadProjectile extends WitherSkull implements AntiMagicSusceptible {
    public CreeperHeadProjectile(EntityType<? extends WitherSkull> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        chainOnKill = false;
    }

    protected float damage;
    protected boolean chainOnKill;

    public CreeperHeadProjectile(LivingEntity shooter, Level level, float speed, float damage) {
        this(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(shooter);

        Vec3 power = shooter.getLookAngle().normalize().scale(speed);

        this.xPower = power.x;
        this.yPower = power.y;
        this.zPower = power.z;
        setDeltaMovement(xPower, yPower, zPower);
        this.damage = damage;
    }

    public CreeperHeadProjectile(LivingEntity shooter, Level level, Vec3 power, float damage) {
        this(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(shooter);

        this.xPower = power.x;
        this.yPower = power.y;
        this.zPower = power.z;
        setDeltaMovement(xPower, yPower, zPower);
        this.damage = damage;
    }

    public void setChainOnKill(boolean chain) {
        chainOnKill = chain;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
    }

    @Override
    public void tick() {
        //super.tick();
//        if (!this.isNoGravity()) {
//            Vec3 vec34 = this.getDeltaMovement();
//            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
//        }
        if (!level().isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                onHit(hitresult);
            }
        } else {
            this.level().addParticle(this.getTrailParticle(), position().x, position().y + 0.25D, position().z, 0.0D, 0.0D, 0.0D);
        }
        ProjectileUtil.rotateTowardsMovement(this, 1);
        setPos(position().add(getDeltaMovement()));

        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }


        this.baseTick();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level().isClientSide) {
            float explosionRadius = 3.5f;
            var entities = level().getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.position().distanceTo(hitResult.getLocation());
                if (distance < explosionRadius) {
                    //Prevent duplicate chains
                    if (entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying() && !canHitEntity(entity))
                        break;
                    float damage = (float) (this.damage * (1 - Math.pow(distance / (explosionRadius), 2)));
                    DamageSources.applyDamage(entity, damage, SpellRegistry.LOB_CREEPER_SPELL.get().getDamageSource(this, getOwner()));
                    entity.invulnerableTime = 0;
                    if (chainOnKill && entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying())
                        ChainCreeperSpell.summonCreeperRing(this.level(), this.getOwner() instanceof LivingEntity livingOwner ? livingOwner : null, livingEntity.getEyePosition(), this.damage * .85f, 3);
                }
            }

            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.0F, false, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
