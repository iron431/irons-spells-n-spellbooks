package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.IHaveSpellList;
import io.redspace.ironsspellbooks.api.spells.ISpellList;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellList;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MagicSwordItem extends ExtendedSwordItem implements IHaveSpellList {

    List<SpellData> spellData = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public MagicSwordItem(Tier tier, double attackDamage, double attackSpeed, SpellDataRegistryHolder[] spellDataRegistryHolders, Map<Attribute, AttributeModifier> additionalAttributes, Properties properties) {
        super(tier, attackDamage, attackSpeed, additionalAttributes, properties);
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
    public ISpellList getSpellList(ItemStack itemStack) {
        if (itemStack == null) {
            return new SpellList();
        }

        if (SpellList.isSpellContainer(itemStack)) {
            return new SpellList(itemStack);
        } else {
            var spellList = new SpellList(spellDataRegistryHolders.length, true, false);
            getSpells().forEach(spellData -> spellList.addSpell(spellData.getSpell(), spellData.getLevel(), true, null));
            spellList.save(itemStack);
            return spellList;
        }
    }
}
