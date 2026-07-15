package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class EvokerSpellBook extends SpellBook {
    public EvokerSpellBook(Properties properties) {
        super(properties.component(SkillcastingDataComponents.SKILL_CONTAINER, ISkillContainer.create(true,
                7,
                new SkillData(SpellRegistry.FANG_STRIKE_SPELL, 6, true),
                new SkillData(SpellRegistry.FANG_WARD_SPELL, 4, true),
                new SkillData(SpellRegistry.SUMMON_VEX_SPELL, 4, true)

        )));
        withSpellbookAttributes(
                new AttributeContainer(AttributeRegistry.EVOCATION_SPELL_POWER, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
