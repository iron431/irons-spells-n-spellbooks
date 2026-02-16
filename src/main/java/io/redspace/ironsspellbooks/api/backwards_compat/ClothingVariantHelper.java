package io.redspace.ironsspellbooks.api.backwards_compat;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ClothingVariantHelper {
    public static final String NBT = "irons_spellbooks:clothing_variant";

    public static @Nullable String getClothingVariant(ItemStack stack) {
        return stack.hasTag() ?
                stack.getOrCreateTag().contains(NBT) ? stack.getOrCreateTag().getString(NBT) :
                        null : null;
    }

    public static void setClothingVariant(ItemStack stack, String value) {
        stack.getOrCreateTag().putString(NBT, value);
    }

    public static String getClothingVariantOrElse(ItemStack stack, String entry) {
        return stack.hasTag() ?
                stack.getOrCreateTag().contains(NBT) ? stack.getOrCreateTag().getString(NBT) :
                        entry : entry;
    }
}
