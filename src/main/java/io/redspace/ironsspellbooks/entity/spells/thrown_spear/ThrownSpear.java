package io.redspace.ironsspellbooks.entity.spells.thrown_spear;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ThrownSpear extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownSpear.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownSpear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ID_CHANNELED = SynchedEntityData.defineId(ThrownSpear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> ID_ITEM = SynchedEntityData.defineId(ThrownSpear.class, EntityDataSerializers.ITEM_STACK);
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public ThrownSpear(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownSpear(Level level, ItemStack spearitem, double damage) {
        this(EntityRegistry.THROWN_SPEAR.get(), level);
        this.setBaseDamage(damage);
        this.setWeaponItem(spearitem);
    }


    public boolean isChanneled() {
        return entityData.get(ID_CHANNELED);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte) 0);
        builder.define(ID_FOIL, false);
        builder.define(ID_ITEM, ItemStack.EMPTY);
        builder.define(ID_CHANNELED, false);
    }

    @Override
    protected void setPickupItemStack(ItemStack pickupItemStack) {
        if (!pickupItemStack.isEmpty()) {
            setWeaponItem(pickupItemStack);
        } else {
            super.setPickupItemStack(pickupItemStack);
        }
    }

    public void setWeaponItem(ItemStack itemStack) {
        this.entityData.set(ID_ITEM, itemStack);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(itemStack));
        this.entityData.set(ID_FOIL, itemStack.hasFoil());
        this.entityData.set(ID_CHANNELED, Utils.getEnchantmentLevel(level, itemStack, Enchantments.CHANNELING) > 0);
    }

    @Override
    protected ItemStack getPickupItem() {
        return getWeaponItem();
    }

    @Override
    public ItemStack getPickupItemStackOrigin() {
        return getWeaponItem();
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.entityData.get(ID_ITEM);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
            this.setNoPhysics(true);
            Vec3 vec3 = entity.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015 * (double) loyalty, this.getZ());
            if (this.level().isClientSide) {
                this.yOld = this.getY();
            }

            double d0 = 0.07 * (double) loyalty;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d0)));
            if (this.clientSideReturnTridentTickCount == 0) {
                // this says client only but it still is completely functional on the server, so...
                this.setDeltaMovement(Vec3.ZERO); // reset momentum on return start for faster return turnaround
                this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
            }
            // help with return hit-reg
            var player = level.getPlayerByUUID(entity.getUUID());
            if (player != null && player.distanceToSqr(this) < Math.clamp(getDeltaMovement().lengthSqr() * 3, 4, 25)) {
                this.playerTouch(player);
            }
            this.clientSideReturnTridentTickCount++;
        }

        super.tick();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    /**
     * Gets the EntityHitResult representing the entity hit
     */
    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHit(HitResult result) {
        if (isChanneled() && !level.isClientSide && !dealtDamage) {
            this.playSound(SoundRegistry.SPEAR_CHANNELING_STRIKE.get(), 6.0F, .9f + Utils.random.nextInt(20) * .01f);
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, getX(), getY(), getZ(), 75, .1, .1, .1, 2, true);
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, getX(), getY(), getZ(), 75, .1, .1, .1, .5, false);
        }
        super.onHit(result);
        // abstract arrow sets sound back to arrow for some reason
        setSoundEvent(SoundEvents.TRIDENT_HIT_GROUND);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity victim = result.getEntity();
        float f = (float) getBaseDamage();
        Entity owner = this.getOwner();
        boolean channeled = isChanneled();
        DamageSource damagesource = channeled ? this.damageSources().source(ISSDamageTypes.LIGHTNING_MAGIC, this, owner == null ? this : owner)
                : this.damageSources().trident(this, owner == null ? this : owner);
        if (this.level() instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), victim, damagesource, f);
        }
        if (channeled && owner instanceof LivingEntity livingOwner) {
            // todo: generic spell power too?
            f *= (float) livingOwner.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER);
        }

        this.dealtDamage = true;
        if (victim.hurt(damagesource, f)) {
            if (victim.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (this.level() instanceof ServerLevel serverlevel1) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, victim, damagesource, this.getWeaponItem());
            }

            if (victim instanceof LivingEntity livingentity) {
                this.doKnockback(livingentity, damagesource);
                this.doPostHurtEffects(livingentity);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.03, -0.1, -0.03));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 0.7F);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult hitResult, ItemStack stack) {
        Vec3 vec3 = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        EnchantmentHelper.onHitBlock(
                level,
                stack,
                this.getOwner() instanceof LivingEntity livingentity ? livingentity : null,
                this,
                null,
                vec3,
                level.getBlockState(hitResult.getBlockPos()),
                p_348680_ -> this.kill()
        );
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (!this.isRemoved() && getOwner() != null && this.ownedBy(player)) {
            int loyalty = this.entityData.get(ID_LOYALTY);
            if ((player.hasInfiniteMaterials() && pickup == Pickup.CREATIVE_ONLY) || (!player.hasInfiniteMaterials() && pickup == Pickup.ALLOWED) || (pickup != Pickup.DISALLOWED && loyalty > 0)) {
                player.getCooldowns().removeCooldown(this.getPickupItem().getItem());
                if (loyalty > 0) {
                    playSound(SoundRegistry.SPEAR_RETURN.get());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void playerTouch(Player entity) {
        if (this.ownedBy(entity) || this.getOwner() == null) {
            super.playerTouch(entity);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("DealtDamage", this.dealtDamage);
        compound.put("item", getWeaponItem().save(this.registryAccess()));
        // fixme: abstractarrow firefromweapon has a really weird and hardcoded codepath... we opt to just ignore it but that likely causes issues with multishot enchantments due to on-projectile-shot enchantment hooks
        compound.remove("weapon");
    }

    private byte getLoyaltyFromItem(ItemStack stack) {
        return this.level() instanceof ServerLevel serverlevel
                ? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverlevel, stack, this), 0, 127)
                : 0;
    }

    @Override
    public void tickDespawn() {
        int i = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || i <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
