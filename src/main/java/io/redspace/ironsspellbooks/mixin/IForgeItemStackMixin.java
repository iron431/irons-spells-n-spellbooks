//package com.example.irons_spellbooks.mixin;
//
//import io.redspace.ironsspellbooks.irons_spellbooks;
//import registries.io.redspace.ironsspellbooks.MobEffectRegistry;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.common.extensions.IForgeItemStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//
//@Mixin(IForgeItemStack.class)
//public interface IForgeItemStackMixin extends IForgeItemStack {
////    @Inject(method = "canElytraFly", at = @At(value = "HEAD"), cancellable = true)
////    default void canElytraFly(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
////        if (entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
////            cir.setReturnValue(true);
////        }
////        cir.cancel();
////    }
//
//    /**
//     * @return True if angel wings effect is active otherwise default behavior
//     * @author Smalls
//     * @reason Angel wings effect handling
//     * @see {@link IForgeItemStack#canElytraFly()}
//     */
//    @Overwrite(remap = false)
//    default boolean canElytraFly(LivingEntity entity) {
//        if (entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
//            irons_spellbooks.LOGGER.debug("canElytraFly called from mixin return true");
//            return true;
//        }
//
//        ItemStack stack = (ItemStack) (Object) this;
//        return stack.getItem().canElytraFly(stack, entity);
//    }
//
//}