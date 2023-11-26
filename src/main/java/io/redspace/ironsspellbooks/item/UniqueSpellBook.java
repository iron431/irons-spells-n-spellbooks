package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    List<SpellData> spellData = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public UniqueSpellBook(SpellRarity rarity, SpellDataRegistryHolder[] spellDataRegistryHolders) {
        super(spellDataRegistryHolders.length, rarity);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public List<SpellData> getSpells() {
        if (spellData == null) {
            spellData = Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellData).toList();
            spellDataRegistryHolders = null;
        }
        return spellData;
    }

    @Override
    public Component getName(ItemStack pStack) {
        var name = super.getName(pStack);
        if (pStack.hasTag() && pStack.getTag().getBoolean("Improved")) {
            return Component.translatable("tooltip.irons_spellbooks.improved_format", name);
        } else {
            return name;
        }
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
