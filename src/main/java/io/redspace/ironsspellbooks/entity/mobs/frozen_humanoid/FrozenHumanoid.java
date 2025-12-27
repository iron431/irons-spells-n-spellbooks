package io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

public class FrozenHumanoid extends LivingEntity implements IEntityWithComplexSpawn {
    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        var owner = getSummoner();
        buffer.writeInt(owner == null ? -1 : owner.getId());
        if (entityToCopy == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(this.entityToCopy));
        }
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        Entity owner = this.level.getEntity(additionalData.readInt());
        if (owner instanceof LivingEntity livingEntity) {
            this.setSummoner(livingEntity);
        }
        if (additionalData.readBoolean()) {
            setEntityTypeToCopy(BuiltInRegistries.ENTITY_TYPE.get(additionalData.readResourceLocation()));
        }
    }

    private class FakeWalkAnimationState extends WalkAnimationState {
        @Override
        public float position() {
            return FrozenHumanoid.this.getWalkAnimPos();
        }

        @Override
        public float position(float partialTick) {
            return FrozenHumanoid.this.getWalkAnimPos();
        }

        @Override
        public float speed() {
            return FrozenHumanoid.this.getWalkAnimSpeed();
        }

        @Override
        public float speed(float partialTick) {
            return FrozenHumanoid.this.getWalkAnimSpeed();
        }
    }

    protected static final EntityDataAccessor<Float> DATA_ATTACK_TIME = SynchedEntityData.defineId(FrozenHumanoid.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> DATA_IS_BABY = SynchedEntityData.defineId(FrozenHumanoid.class, EntityDataSerializers.BOOLEAN);

    /**
     * Client-only value
     */
    float walkAnimSpeed;
    /**
     * Client-only value
     */
    float walkAnimPos;
    private float shatterProjectileDamage;
    private int deathTimer = -1;
    private UUID summonerUUID;
    private LivingEntity cachedSummoner;
    @Nullable EntityType<?> entityToCopy;

    public FrozenHumanoid(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.walkAnimation = new FakeWalkAnimationState();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_ATTACK_TIME, 0f);
        pBuilder.define(DATA_IS_BABY, false);
    }

    private HumanoidArm mainArm = HumanoidArm.RIGHT;

    protected static void copyEntityVisualProperties(LivingEntity baseEntity, LivingEntity entityToCopy) {
        baseEntity.moveTo(entityToCopy.getX(), entityToCopy.getY(), entityToCopy.getZ(), entityToCopy.getYRot(), entityToCopy.getXRot());

        baseEntity.setYBodyRot(entityToCopy.yBodyRot);
        baseEntity.yBodyRotO = baseEntity.yBodyRot;
        baseEntity.setYHeadRot(entityToCopy.getYHeadRot());
        baseEntity.yHeadRotO = baseEntity.yHeadRot;

        baseEntity.setPose(entityToCopy.getPose());
        if (baseEntity instanceof FrozenHumanoid frozenHumanoid) {
            frozenHumanoid.mainArm = entityToCopy.getMainArm();
            frozenHumanoid.getEntityData().set(DATA_ATTACK_TIME, entityToCopy.attackAnim);
            if (entityToCopy.isBaby()) {
                frozenHumanoid.getEntityData().set(DATA_IS_BABY, true);
            }
        } else if (baseEntity.level.isClientSide) {
            baseEntity.walkAnimation = entityToCopy.walkAnimation;
            baseEntity.attackAnim = entityToCopy.attackAnim;
            baseEntity.oAttackAnim = entityToCopy.attackAnim;
            if (entityToCopy.isBaby()) {
                if (baseEntity instanceof AgeableMob ageableMob) {
                    ageableMob.setAge(-10);
                } else if (baseEntity instanceof Zombie zombie) {
                    zombie.setBaby(true);
                }
            }
        }
        if (baseEntity.getAttributes().hasAttribute(Attributes.SCALE) && entityToCopy.getAttributes().hasAttribute(Attributes.SCALE)) {
            baseEntity.getAttributes().getInstance(Attributes.SCALE).setBaseValue(entityToCopy.getAttributeValue(Attributes.SCALE));
        }
        if (entityToCopy instanceof Player player) {
            baseEntity.setCustomName(player.getDisplayName());
            baseEntity.setCustomNameVisible(true);
        }
    }

    @Override
    public boolean isBaby() {
        return entityData.get(DATA_IS_BABY);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return entityToCopy == null ? super.getDefaultDimensions(pose) : entityToCopy.getDimensions();
    }

    public void setEntityTypeToCopy(@Nullable EntityType<?> entityToCopy) {
        this.entityToCopy = entityToCopy;
        refreshDimensions();
    }

    public FrozenHumanoid(Level level, LivingEntity entityToCopy) {
        this(EntityRegistry.FROZEN_HUMANOID.get(), level);
        copyEntityVisualProperties(this, entityToCopy);
        if (!(entityToCopy instanceof Player)) {
            setEntityTypeToCopy(entityToCopy.getType());
        }
        this.invulnerableTime = 1;
        setSummoner(entityToCopy);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public void setTicksFrozen(int ticksFrozen) {
        return;
    }

    public void setSummoner(@javax.annotation.Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    public LivingEntity getSummoner() {
        if (this.cachedSummoner != null && this.cachedSummoner.isAlive()) {
            return this.cachedSummoner;
        } else if (this.summonerUUID != null && this.level() instanceof ServerLevel) {
            if (((ServerLevel) this.level()).getEntity(this.summonerUUID) instanceof LivingEntity livingEntity)
                this.cachedSummoner = livingEntity;
            return this.cachedSummoner;
        } else {
            return null;
        }
    }

    public float getWalkAnimSpeed() {
        return walkAnimSpeed;
    }

    public float getWalkAnimPos() {
        return walkAnimPos;
    }

    @Override
    public void tick() {
        if (firstTick) {
            if (level.isClientSide) {
                if (cachedSummoner != null) {
                    this.walkAnimSpeed = cachedSummoner.walkAnimation.speed();
                    this.walkAnimPos = cachedSummoner.walkAnimation.position();
                }
            }
        }
        super.tick();
        if (deathTimer > 0) {
            deathTimer--;
        }
        if (deathTimer == 0) {
            this.hurt(level().damageSources().generic(), 100);
        }
    }

    public void setDeathTimer(int timeInTicks) {
        this.deathTimer = timeInTicks;
    }

    public float getAttacktime() {
        return this.entityData.get(DATA_ATTACK_TIME);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GLASS_BREAK;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (level().isClientSide || this.isInvulnerableTo(pSource) || invulnerableTime > 0)
            return false;
        invulnerableTime = 10;
        doPuffDamage();
        spawnIcicleShards(this.getEyePosition(), this.shatterProjectileDamage);
        this.playHurtSound(pSource);
        this.discard();
        return true;
    }

    private void doPuffDamage() {
        var damage = this.shatterProjectileDamage * .5f;
        var collider = this.getBoundingBox().inflate(2);
        var radius = collider.getXsize();
        Vec3 center = collider.getCenter();
        var entities = level.getEntities(this, collider);
        for (Entity entity : entities) {
            double distanceSqr = entity.distanceToSqr(center);
            if (distanceSqr < radius * radius && entity.canBeHitByProjectile() && !DamageSources.isFriendlyFireBetween(entity, getSummoner()) && Utils.hasLineOfSight(level, center, entity.getBoundingBox().getCenter(), true)) {
                DamageSources.applyDamage(entity, damage, SpellRegistry.ICICLE_SPELL.get().getDamageSource(this, getSummoner()));
            }
        }
        MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, getX(), getY() + 1, getZ(), 50, 0.2, 0.2, 0.2, 0.2, false);
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, getX(), getY() + 1, getZ(), 50, 0.2, 0.2, 0.2, 0.2, false);
    }

    private void spawnIcicleShards(Vec3 origin, float damage) {
        int count = 8;
        int offset = 360 / count;
        for (int i = 0; i < count; i++) {

            Vec3 motion = new Vec3(0, 0, 1.0);
            motion = motion.xRot(12 * Mth.DEG_TO_RAD);
            motion = motion.yRot(offset * i * Mth.DEG_TO_RAD);


            IcicleProjectile shard = new IcicleProjectile(level(), getSummoner());
            shard.setDamage(damage);
            shard.setDeltaMovement(motion);
            shard.setNoGravity(false);

            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.5f));
            var angle = Utils.rotationFromDirection(motion);

            shard.moveTo(spawn.x, spawn.y - shard.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            level().addFreshEntity(shard);
        }
    }

    public void setShatterDamage(float damage) {
        this.shatterProjectileDamage = damage;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("Summoner")) {
            this.summonerUUID = compoundTag.getUUID("Summoner");
        }
        if (compoundTag.contains("entityToCopy")) {
            try {
                setEntityTypeToCopy(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(compoundTag.getString("entityToCopy"))));
            } catch (Exception ignore) {
            }
        }
        this.deathTimer = compoundTag.getInt("deathTimer");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.summonerUUID != null) {
            compoundTag.putUUID("Summoner", this.summonerUUID);
        }
        if (this.entityToCopy != null) {
            compoundTag.putString("entityToCopy", BuiltInRegistries.ENTITY_TYPE.getKey(entityToCopy).toString());
        }
        compoundTag.putInt("deathTimer", deathTimer);
    }

    @Override
    public HumanoidArm getMainArm() {
        return mainArm;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.FOLLOW_RANGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }
}
