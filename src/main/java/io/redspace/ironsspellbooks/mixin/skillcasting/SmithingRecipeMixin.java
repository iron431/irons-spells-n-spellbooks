package io.redspace.ironsspellbooks.mixin.skillcasting;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.ISkillContainerMutable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(SmithingTransformRecipe.class)
public class SmithingRecipeMixin {
    @WrapMethod(method = "Lnet/minecraft/world/item/crafting/SmithingTransformRecipe;assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;")
    public <T extends ISkillContainer> ItemStack fixSkillContainerCount(SmithingRecipeInput recipe, HolderLookup.Provider registries, Operation<ItemStack> original) {
        ItemStack result = original.call(recipe, registries);
        ItemStack input = recipe.base();
        DataComponentMap resultDefaultComponents = result.getItem().components();
        DataComponentPatch inputComponentPatch = input.getComponentsPatch();
        resultDefaultComponents.stream().forEach(
                typedDataComponent -> {
                    try {
                        if (typedDataComponent.value() instanceof ISkillContainer resultSkillContainer) {
                            DataComponentType<T> type = (DataComponentType<T>) typedDataComponent.type();
                            Optional<? extends T> inputContainer = inputComponentPatch.get(type);
                            if (inputContainer != null && inputContainer.isPresent()) {
                                //copy previous spells using new container vessel
                                ISkillContainerMutable mutable = resultSkillContainer.mutableCopy();
                                for (var slot : inputContainer.get().getActiveSkills()) {
                                    mutable.setSpellAtIndex(slot.skillData(), slot.index());
                                }
                                result.set(type, (T) mutable.toImmutable());
                            }
                        }
                    } catch (Exception e) {
                        Skillcasting.LOGGER.error("Failed to transfer Skill Container data for component {}: {}", BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type()), e.getMessage());
                    }
                }
        );
        return result;
    }
}