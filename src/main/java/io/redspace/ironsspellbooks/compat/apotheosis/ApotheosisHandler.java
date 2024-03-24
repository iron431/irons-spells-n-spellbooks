package io.redspace.ironsspellbooks.compat.apotheosis;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;

public class ApotheosisHandler {
    public static LootCategory SPELLBOOK;

    private static EquipmentSlot[] arr(EquipmentSlot... s) {
        return s;
    }

    public static boolean isSpellbook(ItemStack stack) {
        return LootCategory.forItem(stack).equals(SPELLBOOK);
    }

    public static void init() {
        //If Apothic Curios is not installed, then we need a register a placeholder category.
        //If it is installed, it handles itself and we do nothing
        if(!ModList.get().isLoaded("apothiccurios")) {
            SPELLBOOK = LootCategory.register(LootCategory.SWORD, "curios:spellbook", s -> false, arr());
        }
    }
}
