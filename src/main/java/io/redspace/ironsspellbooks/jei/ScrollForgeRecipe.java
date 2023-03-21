package io.redspace.ironsspellbooks.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ScrollForgeRecipe(List<ItemStack> inkInputs, ItemStack paperInput, ItemStack focusInput, List<ItemStack> scrollOutputs) {
    public ScrollForgeRecipe(List<ItemStack> inkInputs, ItemStack paperInput, ItemStack focusInput, List<ItemStack> scrollOutputs) {
        this.inkInputs = List.copyOf(inkInputs);
        this.paperInput = paperInput;
        this.focusInput = focusInput;
        this.scrollOutputs = List.copyOf(scrollOutputs);
    }

    public boolean isValid() {
        if (inkInputs.isEmpty() || scrollOutputs.isEmpty() || paperInput.isEmpty() || focusInput.isEmpty()) {
            return false;
        }

        return true;
    }
}
