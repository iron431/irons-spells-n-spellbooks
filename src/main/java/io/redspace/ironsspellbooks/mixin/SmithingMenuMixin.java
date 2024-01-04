package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Default priority is 1000
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {
    private static final UpgradeRecipe fakeRecipe = new UpgradeRecipe(new ResourceLocation(""), Ingredient.of(), Ingredient.of(), ItemStack.EMPTY) {
        @Override
        public boolean matches(Container pInv, Level pLevel) {
            return true;
        }
    };

    @Shadow
    private
    UpgradeRecipe selectedRecipe;

    /*
    Necessary to wipe nbt when using shriving stone
    */
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void createResult(CallbackInfo ci) {
        var menu = (SmithingMenu) (Object) this;
        var baseSlot = menu.getSlot(0);
        if (baseSlot.hasItem() && menu.getSlot(1).getItem().getItem().equals(ItemRegistry.SHRIVING_STONE.get())) {
            var resultSlot = menu.getSlot(2);
            ItemStack result = baseSlot.getItem().copy();
            if (result.is(ItemRegistry.SCROLL.get()))
                return;
            boolean flag = false;

            if (ISpellContainer.isSpellContainer(result)) {
                result.removeTagKey(SpellContainer.SPELL_SLOT_CONTAINER);
                flag = true;
            } else if (UpgradeData.hasUpgradeData(result)) {
                UpgradeData.removeUpgradeData(result);
                flag = true;
            }
            if (flag) {
                resultSlot.set(result);
                selectedRecipe = fakeRecipe;
                ci.cancel();
            }
        }
    }
}
