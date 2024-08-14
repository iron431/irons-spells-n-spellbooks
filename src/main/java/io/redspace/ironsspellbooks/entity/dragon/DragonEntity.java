package io.redspace.ironsspellbooks.entity.dragon;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends Mob {

    DragonPartEntity[] subEntities;

    public DragonEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.subEntities = new DragonPartEntity[1];
        subEntities[0] = new DragonPartEntity(this, 3f, 3f);
        this.noCulling = true;
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Copy of forge fix to sub entity id's
    }

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

}
