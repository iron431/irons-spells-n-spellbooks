package io.redspace.ironsspellbooks.api.item;

import net.minecraft.world.item.ItemStack;

public class CastingImplementData {
    public static final String NBT = "irons_spellbooks:casting_implement";

    public static boolean has(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains(NBT);
    }

    public static boolean get(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT);
    }

    public static void set(ItemStack stack, boolean isCastingImplement) {
        stack.getOrCreateTag().putBoolean(NBT, isCastingImplement);
    }
}
