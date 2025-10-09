package io.redspace.ironsspellbooks.mixin;


import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = IForgeItemStack.class, remap = false, priority = 0)
public interface IItemExtensionMixin {

    @Shadow
    ItemStack self();

    @Overwrite(remap = false)
    default boolean canElytraFly(LivingEntity entity) {
        return self().getItem().canElytraFly(self(), entity) || entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get());
    }

    @Overwrite(remap = false)
    default boolean elytraFlightTick(LivingEntity entity, int flightTicks) {
        return self().getItem().elytraFlightTick(self(), entity, flightTicks) || entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get());
    }
//    @Inject(method = "canElytraFly", at = @At(value = "RETURN"), cancellable = true, remap = false)
//    default void canElytraFly(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
//        if (entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
//            cir.setReturnValue(true);
//        }
//    }
//
//    @Inject(method = "elytraFlightTick", at = @At(value = "RETURN"), cancellable = true, remap = false)
//    default void elytraFlightTick(LivingEntity entity, int flightTicks, CallbackInfoReturnable<Boolean> cir) {
//        if (entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
//            cir.setReturnValue(true);
//        }
//    }
}
