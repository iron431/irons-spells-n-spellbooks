package io.redspace.ironsspellbooks.api.item.curios;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AffinityData {
    public static final String ISB_ENHANCE = "ISBEnhance";
    String spellId;
    public static final AffinityData NONE =  new AffinityData(SpellRegistry.none().getSpellId());

    private AffinityData(String id) {
        this.spellId = id;
    }

    public static AffinityData getAffinityData(ItemStack stack) {
        if (hasAffinityData(stack)) {
            return new AffinityData(stack.getOrCreateTag().getString(ISB_ENHANCE));
        } else {
            return NONE;
        }
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell spell) {
        var spellTag = stack.getOrCreateTag();
        spellTag.putString(ISB_ENHANCE, spell.getSpellId());
    }

    public static boolean hasAffinityData(ItemStack itemStack) {
        return itemStack.getTag() != null && itemStack.getTag().contains(ISB_ENHANCE);
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellId);
    }

    public String getNameForItem(){
        return getSpell() == SpellRegistry.none() ? Component.translatable("tooltip.irons_spellbooks.no_affinity").getString() : getSpell().getSchoolType().getDisplayName().getString();
    }
}
