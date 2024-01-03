package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.IContainSpells;
import io.redspace.ironsspellbooks.api.spells.ISpellSlotContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.api.spells.SpellSlotContainer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MagicSwordItem extends ExtendedSwordItem implements IContainSpells {

    List<SpellSlot> spellSlots = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;


//    public AbstractSpell getImbuedSpell() {
//        return holder.getSpellSlot().getSpell();
//    }
//
//    public int getImbuedLevel() {
//        return holder.getSpellSlot().getLevel();
//    }

    public MagicSwordItem(Tier tier, double attackDamage, double attackSpeed, SpellDataRegistryHolder[] spellDataRegistryHolders, Map<Attribute, AttributeModifier> additionalAttributes, Properties properties) {
        super(tier, attackDamage, attackSpeed, additionalAttributes, properties);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public List<SpellSlot> getSpells() {
        if (spellSlots == null) {
            spellSlots = Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellSlot).toList();
            spellDataRegistryHolders = null;
        }
        return spellSlots;
    }


    @Override
    public ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return new SpellSlotContainer();
        }

        if (SpellSlotContainer.isSpellContainer(itemStack)) {
            return new SpellSlotContainer(itemStack);
        } else {
            var ssc = new SpellSlotContainer(spellDataRegistryHolders.length, true, false);
            getSpells().forEach(spellSlot -> ssc.addSpellToOpenSlot(spellSlot.getSpell(), spellSlot.getLevel(), true, null));
            ssc.save(itemStack);
            return ssc;
        }
    }
}
