package io.redspace.ironsspellbooks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmithingTransformRecipe.class)
public class SmithingRecipeMixin {
    @WrapMethod(method = "Lnet/minecraft/world/item/crafting/SmithingTransformRecipe;assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;")
    public ItemStack fixDyedUpgrades(SmithingRecipeInput recipe, HolderLookup.Provider registries, Operation<ItemStack> original) {
        ItemStack result = original.call(recipe, registries);
        ItemStack input = recipe.base();
        if (input.is(ItemTags.DYEABLE) && !result.is(ItemTags.DYEABLE) && input.has(DataComponents.DYED_COLOR)) {
            result.remove(DataComponents.DYED_COLOR);
        }
        return result;
    }
}