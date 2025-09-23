package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.weapons.IronsWeaponTier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MagicSwordItem extends ExtendedSwordItem implements IPresetSpellContainer {

    List<SpellData> spellData = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public <T extends Tier & IronsWeaponTier> MagicSwordItem(T pTier, Properties pProperties, SpellDataRegistryHolder[] spellDataRegistryHolders) {
        super(pTier, pProperties);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    /**
     * Use {@link IronsWeaponTier} and {@link MagicSwordItem#MagicSwordItem(Tier, Properties, SpellDataRegistryHolder[])}
     */
    @Deprecated(forRemoval = true)
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
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            var spells = getSpells();
            var spellContainer = ISpellContainer.create(spells.size(), true, false).mutableCopy();
            spells.forEach(spellData -> spellContainer.addSpell(spellData.getSpell(), spellData.getLevel(), true));
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
    }
}
