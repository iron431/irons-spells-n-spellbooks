package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NecronomiconSpellBook extends UniqueSpellBook {
    public NecronomiconSpellBook() {
        super(SpellDataRegistryHolder.of(
                new SpellDataRegistryHolder(SpellRegistry.BLOOD_SLASH_SPELL, 5),
                new SpellDataRegistryHolder(SpellRegistry.BLOOD_STEP_SPELL, 5),
                new SpellDataRegistryHolder(SpellRegistry.RAY_OF_SIPHONING_SPELL, 5),
                new SpellDataRegistryHolder(SpellRegistry.BLAZE_STORM_SPELL, 5)
        ), 6);
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        var affinityData = AffinityData.getAffinityData(itemStack);
        var spell = affinityData.getSpell();
        if (spell != SpellRegistry.none()) {
            int i = TooltipsUtils.indexOfComponent(lines, "tooltip.irons_spellbooks.spellbook_spell_count");
            lines.add(i < 0 ? lines.size() : i + 1, Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", spell.getDisplayName(MinecraftInstanceHelper.instance.player()).withStyle(spell.getSchoolType().getDisplayName().getStyle())).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        super.initializeSpellContainer(itemStack);
        AffinityData.setAffinityData(itemStack, SpellRegistry.RAISE_DEAD_SPELL.get());
    }
}
