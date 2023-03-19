package io.redspace.ironsspellbooks.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ArcaneAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
    public ArcaneAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        this.leftInputs = List.copyOf(leftInputs);
        this.rightInputs = List.copyOf(rightInputs);
        this.outputs = List.copyOf(outputs);
    }
}
