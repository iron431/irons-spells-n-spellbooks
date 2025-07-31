package io.redspace.ironsspellbooks.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker(value = "setLivingEntityFlag", remap = false)
    void setLivingEntityFlagInvoker(int key, boolean value);

}