package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public class SmithingRecipeMixin {
    /**
     * Due to spell containers having highly item-specific external parameters (equip status, max slots, etc),
     * it is imperative that when copied, only their contents (spells) are copied, not the container itself.
     * Always defer to the container of the resulting item.
     * <p>
     * Also fix dye status copying to non-dyeable items because that too
     */
    @Inject(method = "Lnet/minecraft/world/item/crafting/SmithingTransformRecipe;assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN"), cancellable = true)
    public void fixSpellbookSlotCount(SmithingRecipeInput pInput, HolderLookup.Provider pRegistries, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        ItemStack input = pInput.base();
        ISpellContainer defaultResultContainer = result.getItem().getDefaultInstance().get(ComponentRegistry.SPELL_CONTAINER);
        ISpellContainer baseContainer = input.get(ComponentRegistry.SPELL_CONTAINER);
        if (defaultResultContainer != null && baseContainer != null) {
            //copy previous spells using new container vessel
            var mutable = defaultResultContainer.mutableCopy();
            for (SpellSlot slot : baseContainer.getActiveSpells()) {
                mutable.addSpellAtIndex(slot.getSpell(), slot.getLevel(), slot.index(), slot.isLocked());
            }
            ISpellContainer.set(result, mutable.toImmutable());
            cir.setReturnValue(result);
        }

        if (input.is(ItemTags.DYEABLE) && !result.is(ItemTags.DYEABLE) && input.has(DataComponents.DYED_COLOR)) {
            result.remove(DataComponents.DYED_COLOR);
            cir.setReturnValue(result);
        }
    }
}