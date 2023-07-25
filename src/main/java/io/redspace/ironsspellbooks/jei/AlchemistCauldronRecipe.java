package io.redspace.ironsspellbooks.jei;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public record AlchemistCauldronRecipe(List<ItemStack> inputs, List<ItemStack> outputs, List<ItemStack> catalysts) {
    public AlchemistCauldronRecipe(List<ItemStack> inputs, List<ItemStack> outputs, List<ItemStack> catalysts) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.catalysts = List.copyOf(catalysts);
    }
}
