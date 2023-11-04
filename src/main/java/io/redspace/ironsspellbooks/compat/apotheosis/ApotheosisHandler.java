package io.redspace.ironsspellbooks.compat.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ApotheosisHandler {

    public static final LootCategory SPELLBOOK = LootCategory.register(LootCategory.SWORD, "spellbook", s-> s.getItem() instanceof SpellBook, arr(EquipmentSlot.MAINHAND));

    private static EquipmentSlot[] arr(EquipmentSlot... s) {
        return s;
    }

    public static boolean isSpellbook(ItemStack stack) { return LootCategory.forItem(stack).equals(SPELLBOOK); }

    public static void init() {}
}
