package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NecronomiconSpellBook extends SpellBook {
    public NecronomiconSpellBook(Item.Properties properties) {
        super(properties.component(SkillcastingDataComponents.SKILL_CONTAINER, SkillContainer.create(true,
                6,
                new SkillData(SkillRegistry.BLOOD_SLASH_SPELL, 5),
                new SkillData(SkillRegistry.BLOOD_STEP_SPELL, 5),
                new SkillData(SkillRegistry.RAY_OF_SIPHONING_SPELL, 5),
                new SkillData(SkillRegistry.BLAZE_STORM_SPELL, 5)

        )));
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        var affinityData = AffinityData.getAffinityData(itemStack);
        if (!affinityData.affinityData().isEmpty()) {
            int i = TooltipsUtils.indexOfComponent(lines, "tooltip.irons_spellbooks.spellbook_spell_count");
            lines.addAll(i < 0 ? lines.size() : i + 1, affinityData.getDescriptionComponent());
        }
    }

    // fixme: affinity data
//    @Override
//    public void initializeSpellContainer(ItemStack itemStack) {
//        if (itemStack == null) {
//            return;
//        }
//
//        super.initializeSpellContainer(itemStack);
//        AffinityData.setAffinityData(itemStack, SkillRegistry.RAISE_DEAD_SPELL.get(), 2);
//    }
}
