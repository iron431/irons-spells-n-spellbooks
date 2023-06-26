package io.redspace.ironsspellbooks.api.item.curios;

import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.item.ItemStack;

public class RingData {
    public static final String nbtKey = "ISBEnhance";
    SpellType spell;

    private RingData(int id) {
        this.spell = SpellType.getTypeFromValue(id);
    }

    public static RingData getRingData(ItemStack stack) {
        if (hasRingData(stack)) {
            return new RingData(stack.getOrCreateTag().getInt(nbtKey));
        } else {
            return new RingData(0);
        }
    }

    public static void setRingData(ItemStack stack, SpellType spell) {
        var spellTag = stack.getOrCreateTag();
        spellTag.putInt(nbtKey, spell.getValue());
    }

    public static boolean hasRingData(ItemStack stack) {
        return stack.getOrCreateTag().contains(nbtKey);
    }

    public SpellType getSpell() {
        return spell;
    }
}
