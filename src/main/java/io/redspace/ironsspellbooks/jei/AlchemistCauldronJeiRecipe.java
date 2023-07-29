package io.redspace.ironsspellbooks.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AlchemistCauldronJeiRecipe(List<ItemStack> inputs, List<ItemStack> outputs, List<ItemStack> catalysts) {
    public AlchemistCauldronJeiRecipe(List<ItemStack> inputs, List<ItemStack> outputs, List<ItemStack> catalysts) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.catalysts = List.copyOf(catalysts);
    }
}
