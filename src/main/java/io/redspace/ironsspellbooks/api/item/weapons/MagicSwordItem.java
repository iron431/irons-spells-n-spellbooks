package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.world.item.Tier;

@Deprecated
public class MagicSwordItem extends ExtendedSwordItem {
    public MagicSwordItem(Tier pTier, Properties pProperties, SkillData... spellDataRegistryHolders) {
        super(pTier, pProperties.component(SkillcastingDataComponents.SKILL_CONTAINER, ISkillContainer.create(false, spellDataRegistryHolders)));
    }
}
