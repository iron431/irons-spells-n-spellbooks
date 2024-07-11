package io.redspace.ironsspellbooks.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
//    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
//    public void init(ItemLike itemLike, int count, CompoundTag capNBT, CallbackInfo ci) {
//        if (itemLike != null && itemLike.asItem() instanceof IPresetSpellContainer iPresetSpellContainer) {
//            iPresetSpellContainer.initializeSpellContainer((ItemStack) (Object) this);
//        }
//    }
}