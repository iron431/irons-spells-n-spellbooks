package io.redspace.ironsspellbooks.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("removalReason")
    void setRemovalReason(Entity.RemovalReason reason);
}
