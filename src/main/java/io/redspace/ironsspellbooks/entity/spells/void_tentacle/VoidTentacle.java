package io.redspace.ironsspellbooks.entity.spells.void_tentacle;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public class VoidTentacle extends LivingEntity implements IAnimatable, AntiMagicSusceptible {
    //private static final EntityDataAccessor<Integer> DATA_DELAY = SynchedEntityData.defineId(VoidTentacle.class, EntityDataSerializers.INT);

    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;
    private float damage;
    private int age;

    public VoidTentacle(EntityType<? extends VoidTentacle> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public VoidTentacle(Level level, LivingEntity owner, float damage) {
        this(EntityRegistry.SCULK_TENTACLE.get(), level);
        setOwner(owner);
        setDamage(damage);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if (age > 300) {
                //IronsSpellbooks.LOGGER.debug("Discarding void Tentacle (age:{})", age);
                this.discard();
            } else {
                if (age < 280 && (age) % 20 == 0) {
                    level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.2)).forEach(this::dealDamage);
                    if (level.random.nextFloat() < .15f)
                        playSound(SoundRegistry.VOID_TENTACLES_AMBIENT.get(), 1.5f, .5f + level.random.nextFloat() * .65f);
                }
            }
            if (age == 260 && level.random.nextFloat() < .3f)
                playSound(SoundRegistry.VOID_TENTACLES_LEAVE.get(), 2, 1);
        } else {
            if (age < 280)
//                for (int i = 0; i < 4; i++) {
                if (level.random.nextFloat() < .15f)
                    level.addParticle(ParticleHelper.VOID_TENTACLE_FOG, getX() + Utils.getRandomScaled(.5f), getY() + Utils.getRandomScaled(.5f) + .2f, getZ() + Utils.getRandomScaled(.5f), Utils.getRandomScaled(2f), -random.nextFloat() * .5f, Utils.getRandomScaled(2f));
//                }
        }
        age++;

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public boolean dealDamage(LivingEntity target) {
        if (target != getOwner())
            if (DamageSources.applyDamage(target, damage, SpellRegistry.SCULK_TENTACLES_SPELL.get().getDamageSource(this, getOwner()), SpellRegistry.SCULK_TENTACLES_SPELL.get().getSchoolType())) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                return true;
            }
        return false;
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        this.owner = pOwner;
        this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!pSource.isBypassInvul())
            return false;
        return super.hurt(pSource, pAmount);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
            Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        return this.owner;
    }

//    @Override
//    public boolean isInvisible() {
//        return super.isInvisible() || age < clientDelay;
//    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.age = pCompound.getInt("Age");
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
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

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Age", this.age);
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        MagicManager.spawnParticles(level, ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 50, .2, 1.25, .2, .08, false);
        this.discard();
    }


//    @Override
//    protected void defineSynchedData() {
//        //entityData.define(DATA_DELAY, this.random.nextIntBetweenInclusive(0, 10));
//        //IronsSpellbooks.LOGGER.debug("VoidTentacle delay: {}", getDelay());
//
//    }

//    private int getDelay() {
//        return entityData.get(DATA_DELAY);
//    }

    private PlayState animationPredicate(AnimationEvent event) {
        //if (age >= getDelay()) {
        var controller = event.getController();
        //if (controller.getAnimationState() == AnimationState.Stopped) {
        //}
        //IronsSpellbooks.LOGGER.debug("TentacleAnimOffset: {}", controller.tickOffset);
        if (age > 220 && level.random.nextFloat() < .04f) {
            controller.setAnimation(ANIMATION_RETREAT);
        } else if (controller.getAnimationState() == AnimationState.Stopped) {
//            controller.setAnimationSpeed((2 + this.level.random.nextFloat()) / 2f);
//            int animation = random.nextInt(3);
//            //IronsSpellbooks.LOGGER.debug("Choosing new animation ({})", animation);
//            switch (animation) {
//                case 0 -> controller.setAnimation(ANIMATION_FLAIL);
//                case 1 -> controller.setAnimation(ANIMATION_FLAIL2);
//                case 2 -> controller.setAnimation(ANIMATION_FLAIL3);
//            }
            controller.setAnimation(ANIMATION_IDLE);
        }

        return PlayState.CONTINUE;
        //}

        //return PlayState.STOP;
    }

    private PlayState risePredicate(AnimationEvent event) {
        //if (age >= getDelay()) {
        var controller = event.getController();
        //if (controller.getAnimationState() == AnimationState.Stopped) {
        //}
        //IronsSpellbooks.LOGGER.debug("TentacleAnimOffset: {}", controller.tickOffset);
        if (age < 10) {
            controller.setAnimation(ANIMATION_RISE);
            return PlayState.CONTINUE;
        } else
            return PlayState.STOP;


    }


    private final AnimationBuilder ANIMATION_RISE = new AnimationBuilder().addAnimation("rise", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder ANIMATION_RETREAT = new AnimationBuilder().addAnimation("retreat", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    private final AnimationBuilder ANIMATION_IDLE = new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    private final AnimationBuilder ANIMATION_FLAIL = new AnimationBuilder().addAnimation("flail", ILoopType.EDefaultLoopTypes.LOOP);
    private final AnimationBuilder ANIMATION_FLAIL2 = new AnimationBuilder().addAnimation("flail2", ILoopType.EDefaultLoopTypes.LOOP);
    private final AnimationBuilder ANIMATION_FLAIL3 = new AnimationBuilder().addAnimation("flail3", ILoopType.EDefaultLoopTypes.LOOP);
    private final AnimationController controller = new AnimationController(this, "void_tentacle_controller", 20, this::animationPredicate);
    private final AnimationController riseController = new AnimationController(this, "void_tentacle_rise_controller", 0, this::risePredicate);


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(riseController);
        data.addAnimationController(controller);
    }

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
