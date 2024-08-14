package io.redspace.ironsspellbooks.entity.dragon;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericFollowOwnerGoal;
import io.redspace.ironsspellbooks.network.mob.DragonSyncWalkStatePacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends PathfinderMob implements IClientEventEntity {


    DragonPartEntity[] subEntities;
    DragonPartEntity leftLeg;
    DragonPartEntity rightLeg;
//    public final WalkAnimationState dragonWalkAnimationState = new WalkAnimationState();

    public DragonEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.leftLeg = new DragonPartEntity(this, 1f, 2f);
        this.rightLeg = new DragonPartEntity(this, 1f, 2f);
        this.subEntities = new DragonPartEntity[]{
                new DragonPartEntity(this, 3f, 3f),
                leftLeg,
                rightLeg
        };
        this.noCulling = true;
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Copy of forge fix to sub entity id's
        this.walkAnimation = new WalkAnimationState() {
            @Override
            public void setSpeed(float pSpeed) {
                if (pSpeed != 1.5f) {
                    super.setSpeed(pSpeed);
                }
            }
        };

    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10)
                .add(Attributes.MOVEMENT_SPEED, .25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new GenericFollowOwnerGoal(this, () -> level.getNearestPlayer(TargetingConditions.forNonCombat().ignoreLineOfSight().range(16), this), 1, 6, 5, false, 999));
    }

//    @Override
//    protected void updateWalkAnimation(float pPartialTick) {
//        float f = Math.min(pPartialTick * 4.0F, 1.0F);
//        this.dragonWalkAnimationState.update(f, 0.4F);
//    }

    //    @Override
//    public boolean isPushable() {
//        return false;
//    }

    @Override
    public void setId(int id) {
        //Allows sub entities to transfer melee damage
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) {
            this.subEntities[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return subEntities;
    }


    @Override
    public void tick() {
        super.tick();
        if (tickCount % 80 == 0) {
            if (!level.isClientSide) {
                PacketDistributor.sendToPlayersTrackingEntity(this, new DragonSyncWalkStatePacket(this));
            }
        }
        if (tickCount % 20 == 0) {
            IronsSpellbooks.LOGGER.debug("DragonWalk: {} {}", this.walkAnimation.position(), this.walkAnimation.speed());
        }
        /*
        Animation Notes:
        #start will override current animation state and start the animation from the beginning
        #startIfStopped only starts if the animation state is stopped
        animation states do not automatically stop when the animation is over
        animation states will hold their last pose until #stop is explicitly called
         */
        if (level.isClientSide) {
            //testAnimationState.startIfStopped(this.tickCount);
            if (!this.onGround()) {
                testAnimationState.start(this.tickCount);
            }

            //if (testAnimationState.isStarted() && testAnimationState.getAccumulatedTime() > TestDragonAnimation.test_animation.lengthInSeconds() * 1000f) {
            //    testAnimationState.stop();
            //}
        }

        float scale = 1f;
        Vec3 pos = this.getBoundingBox().getCenter();
        for (int i = 0; i < subEntities.length; i++) {
            var subEntity = subEntities[i];

            double distance = 1 + (i * scale * subEntity.getDimensions(null).width() / 2);
            Vec3 newVector = pos;
            subEntity.setPos(newVector);
            subEntity.setDeltaMovement(newVector);
            var vec3 = new Vec3(subEntity.getX(), subEntity.getY(), subEntity.getZ());
            subEntity.xo = vec3.x;
            subEntity.yo = vec3.y;
            subEntity.zo = vec3.z;
            subEntity.xOld = vec3.x;
            subEntity.yOld = vec3.y;
            subEntity.zOld = vec3.z;
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return !this.level.isClientSide && this.hurt(subEntities[0], pSource, pAmount);
    }

    public boolean hurt(DragonPartEntity bodypart, DamageSource source, float amount) {
        //todo: can do cool damage manipulations based on bodypart (ie headshots)
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

//    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        IronsSpellbooks.LOGGER.debug("{}", this.tickCount);
//        if(level.isClientSide){
//            testAnimationState.start(this.tickCount);
//        }
//        return super.hurt(pSource, pAmount);
//    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public final AnimationState testAnimationState = new AnimationState();

    @Override
    public void handleClientEvent(byte eventId) {

    }
}
