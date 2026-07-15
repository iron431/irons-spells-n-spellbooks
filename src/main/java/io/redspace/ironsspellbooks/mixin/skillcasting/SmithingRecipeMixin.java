package io.redspace.ironsspellbooks.mixin.skillcasting;

import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public class SmithingRecipeMixin {
    @Inject(method = "Lnet/minecraft/world/item/crafting/SmithingTransformRecipe;assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN"), cancellable = true)
    public void fixSkillContainerCount(SmithingRecipeInput pInput, HolderLookup.Provider pRegistries, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        ItemStack input = pInput.base();
        ISkillContainer defaultResultContainer = result.getItem().getDefaultInstance().get(SkillcastingDataComponents.SKILL_CONTAINER);
        ISkillContainer baseContainer = input.get(SkillcastingDataComponents.SKILL_CONTAINER);
        if (defaultResultContainer != null && baseContainer != null) {
            //copy previous spells using new container vessel
            var mutable = defaultResultContainer.mutableCopy();
            for (var slot : baseContainer.getActiveSkills()) {
                mutable.setSpellAtIndex(slot.skillData(), slot.index());
            }
            ISkillContainer.set(result, mutable.toImmutable());
            cir.setReturnValue(result);
        }
    }
}