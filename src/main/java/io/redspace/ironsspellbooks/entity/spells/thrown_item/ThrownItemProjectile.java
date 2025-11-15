package io.redspace.ironsspellbooks.entity.spells.thrown_item;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ThrownItemProjectile extends AbstractMagicProjectile {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ThrownItemProjectile.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(ThrownItemProjectile.class, EntityDataSerializers.FLOAT);

    public float getScale() {
        return entityData.get(DATA_SCALE);
    }

    public void setScale(float scale) {
        entityData.set(DATA_SCALE,scale);
    }

    public ThrownItemProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownItemProjectile(Level level, ItemStack itemStack) {
        this(EntityRegistry.THROWN_ITEM.get(), level);
        setThrownItem(itemStack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_ITEM, ItemStack.EMPTY);
        pBuilder.define(DATA_SCALE, 1f);
    }

    public ItemStack getThrownItem() {
        return entityData.get(DATA_ITEM);
    }

    public void setThrownItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        var item = getThrownItem();
        if (!item.isEmpty()) {
            tag.put("item", item.save(this.level.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("item")) {
            this.setThrownItem(ItemStack.parseOptional(level.registryAccess(), tag.getCompound("item")));
        }
    }

    @Override
    public void trailParticles() {

    }

    @Nullable
    @Override
    public ItemStack getWeaponItem() {
        return getThrownItem();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        var item = getThrownItem();
        double damage = getDamage();
        var target = pResult.getEntity();
        var damageSource = SpellRegistry.THROW_SPELL.get().getDamageSource(this, getOwner());
        if (DamageSources.applyDamage(target, (float) damage, damageSource) && !item.isEmpty() && level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, damageSource, item);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        //todo: stick into blocks?
        discard();
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleTypes.CRIT, x, y, z, 25, 0.1, 0.1, 0.1, 0.5, true);
    }

    @Override
    public float getSpeed() {
        return 1.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.TRIDENT_HIT_GROUND));
    }
}
