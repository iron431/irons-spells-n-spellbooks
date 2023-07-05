package io.redspace.ironsspellbooks.api.item.curios;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractCollection;

public class RingData {
    public static final String nbtKey = "ISBEnhance";
    String spellId;

    private RingData(String id) {
        this.spellId = id;
    }

    public static RingData getRingData(ItemStack stack) {
        if (hasRingData(stack)) {
            return new RingData(stack.getOrCreateTag().getString(nbtKey));
        } else {
            return new RingData(SpellRegistry.none().getSpellId());
        }
    }

    public static void setRingData(ItemStack stack, AbstractSpell spell) {
        var spellTag = stack.getOrCreateTag();
        spellTag.putString(nbtKey, spell.getSpellId());
    }

    public static boolean hasRingData(ItemStack stack) {
        return stack.getOrCreateTag().contains(nbtKey);
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellId);
    }
}
