package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ExtendedFireworkRocket extends FireworkRocketEntity implements AntiMagicSusceptible {
    protected static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(ExtendedFireworkRocket.class, EntityDataSerializers.ITEM_STACK);

    public ExtendedFireworkRocket(Level pLevel, ItemStack pStack, Entity pShooter, double pX, double pY, double pZ, boolean pShotAtAngle, float damage) {
        super(pLevel, pStack, pShooter, pX, pY, pZ, pShotAtAngle);
        this.damage = damage;
    }

    private final float damage;

    public float getDamage() {
        return damage;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            this.explode();
        }
    }

    private void explode() {
        //Copied from private FireworkRocketEntity explode
        this.level.broadcastEntityEvent(this, (byte) 17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage();
        this.discard();
    }

    private void dealExplosionDamage() {
        //Copied from private FireworkRocketEntity dealExplosionDamage
        Vec3 pos = this.position();
        boolean los = false;
        double explosionRadius = 2;
        for (LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(explosionRadius))) {
            if (this.distanceToSqr(livingentity) <= explosionRadius * explosionRadius  && canHitEntity(livingentity)) {

                for (int i = 0; i < 2; ++i) {
                    Vec3 targetPos = new Vec3(livingentity.getX(), livingentity.getY(0.5D * (double) i), livingentity.getZ());
                    HitResult hitresult = this.level.clip(new ClipContext(pos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (hitresult.getType() == HitResult.Type.MISS) {
                        los = true;
                        break;
                    }
                }

                if (los) {
                    DamageSources.applyDamage(livingentity, this.getDamage(), SpellType.FIRECRACKER_SPELL.getDamageSource(this,getOwner()), SchoolType.EVOCATION);
                }
            }
        }

    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
