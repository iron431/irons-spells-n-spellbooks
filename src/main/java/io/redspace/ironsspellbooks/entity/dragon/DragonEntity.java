package io.redspace.ironsspellbooks.entity.dragon;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class DragonEntity extends Mob {

    public DragonEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
