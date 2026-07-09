package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.world.item.Tier;

@Deprecated
public class MagicSwordItem extends ExtendedSwordItem {

//    List<SkillData> spellData = null;
//    SkillData[] spellDataRegistryHolders;

    public MagicSwordItem(Tier pTier, Properties pProperties, SkillData... spellDataRegistryHolders) {
        super(pTier, pProperties.component(SkillcastingDataComponents.SKILL_CONTAINER, ISkillContainer.create(false, spellDataRegistryHolders)));
//        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

//    public List<SkillData> getSpells() {
//        if (spellData == null) {
//            spellData = List.of(spellDataRegistryHolders);
//        }
//        return spellData;
//    }

//    @Override
//    public void initializeSpellContainer(ItemStack itemStack) {
//        if (itemStack == null) {
//            return;
//        }
//
//        if (!ISpellContainer.isSpellContainer(itemStack)) {
//            var spells = getSpells();
//            var spellContainer = ISpellContainer.create(spells.size(), true, false).mutableCopy();
//            spells.forEach(spellData -> spellContainer.addSpell(spellData.getSkill(), spellData.getLevel(), true));
//            ISpellContainer.set(itemStack, spellContainer.toImmutable());
//        }
//    }
}
