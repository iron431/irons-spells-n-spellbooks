package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    public void init(ItemLike itemLike, int pCount, PatchedDataComponentMap pComponents, CallbackInfo ci) {
        if (itemLike instanceof IPresetSpellContainer iPresetSpellContainer) {
            iPresetSpellContainer.initializeSpellContainer((ItemStack) (Object) this);
        }
    }
}