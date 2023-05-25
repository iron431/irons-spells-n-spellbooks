package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.item.ItemStack;

public class RingData {
    SpellType spell;

    private RingData(int id) {
        this.spell = SpellType.getTypeFromValue(id);
    }

    public static RingData getRingData(ItemStack stack) {
        if (hasRingData(stack)) {
            return new RingData(stack.getOrCreateTag().getInt(CasterRing.nbtKey));
        } else {
            return new RingData(0);
        }
    }

    public static void setRingData(ItemStack stack, SpellType spell) {
        var spellTag = stack.getOrCreateTag();
        spellTag.putInt(CasterRing.nbtKey, spell.getValue());
    }

    public static boolean hasRingData(ItemStack stack) {
        return stack.getOrCreateTag().contains(CasterRing.nbtKey);
    }

    public SpellType getSpell() {
        return spell;
    }
}
