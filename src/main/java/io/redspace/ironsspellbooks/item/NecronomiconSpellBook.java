package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class NecronomiconSpellBook extends SpellBook {
    public NecronomiconSpellBook(Item.Properties properties) {
        super(properties.component(ComponentRegistry.SPELLBOOK_CONTAINER, ISkillContainer.create(true,
                6,
                new SkillData(SpellRegistry.BLOOD_SLASH_SPELL, 5, true),
                new SkillData(SpellRegistry.BLOOD_STEP_SPELL, 5, true),
                new SkillData(SpellRegistry.RAY_OF_SIPHONING_SPELL, 5, true),
                new SkillData(SpellRegistry.BLAZE_STORM_SPELL, 5, true)

        )).component(ComponentRegistry.AFFINITY_COMPONENT.get(), AffinityData.ofHolders(Map.of(
                SpellRegistry.RAISE_DEAD_SPELL, 2
        ))));
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
}
