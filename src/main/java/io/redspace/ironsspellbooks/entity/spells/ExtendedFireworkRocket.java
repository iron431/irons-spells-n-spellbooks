package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
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
import net.minecraft.world.phys.AABB;
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
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        explode();
    }

    private void explode() {
        //Copied from private FireworkRocketEntity explode
        this.level.broadcastEntityEvent(this, (byte) 17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage();
        this.discard();
    }

    private void dealExplosionDamage() {
        Vec3 hitPos = this.position();
        double explosionRadius = 2;
        for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, new AABB(hitPos.subtract(explosionRadius, explosionRadius, explosionRadius), hitPos.add(explosionRadius, explosionRadius, explosionRadius)))) {
            if (livingentity.isAlive() && livingentity.isPickable() && Utils.hasLineOfSight(level, hitPos, livingentity.getBoundingBox().getCenter(), true)) {
                DamageSources.applyDamage(livingentity, this.getDamage(), SpellRegistry.FIRECRACKER_SPELL.get().getDamageSource(this, getOwner()));
            }
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
