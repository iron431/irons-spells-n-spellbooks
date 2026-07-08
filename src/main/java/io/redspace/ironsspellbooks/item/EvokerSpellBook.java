package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class EvokerSpellBook extends SpellBook {
    public EvokerSpellBook(Properties properties) {
        super(properties.component(SkillcastingDataComponents.SKILL_CONTAINER, SkillContainer.create(true,
                7,
                new SkillData(SkillRegistry.FANG_STRIKE_SPELL, 6),
                new SkillData(SkillRegistry.FANG_WARD_SPELL, 4),
                new SkillData(SkillRegistry.SUMMON_VEX_SPELL, 4)

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
