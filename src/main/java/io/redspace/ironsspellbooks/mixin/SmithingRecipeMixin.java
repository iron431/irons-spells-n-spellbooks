package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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
    @Inject(method = "assemble", at = @At(value = "RETURN"), cancellable = true)
    public void fixSpellbookSlotCount(Container pContainer, RegistryAccess pRegistryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        ItemStack input = pContainer.getItem(1);
        ISpellContainer defaultResultContainer = ISpellContainer.get(result.getItem().getDefaultInstance());
        ISpellContainer baseContainer = ISpellContainer.get(input);
        if (defaultResultContainer != null && baseContainer != null) {
            //copy previous spells using new container vessel
            var mutable = defaultResultContainer.mutableCopy();
            for (SpellSlot slot : baseContainer.getActiveSpells()) {
                mutable.addSpellAtIndex(slot.getSpell(), slot.getLevel(), slot.index(), slot.isLocked());
            }
            ISpellContainer.set(result, mutable.toImmutable());
            cir.setReturnValue(result);
        } else if (defaultResultContainer != null) {
            // 1.20.1 only due to harsh tag overriding
            ISpellContainer.set(result, defaultResultContainer);
        }

//        if (input.is(ItemTags.DYEABLE) && !result.is(ItemTags.DYEABLE) && input.has(DataComponents.DYED_COLOR)) {
//            result.remove(DataComponents.DYED_COLOR);
//            cir.setReturnValue(result);
//        }
    }
}