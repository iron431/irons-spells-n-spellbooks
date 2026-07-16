package io.redspace.ironsspellbooks.api.item.weapons;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import net.minecraft.world.item.Tier;

@Deprecated
public class MagicSwordItem extends ExtendedSwordItem {
    public MagicSwordItem(Tier pTier, Properties pProperties, SkillData... spellDataRegistryHolders) {
        super(pTier, pProperties.component(ComponentRegistry.IMBUED_SPELL_CONTAINER, ISkillContainer.create(false, spellDataRegistryHolders)));
    }
}
