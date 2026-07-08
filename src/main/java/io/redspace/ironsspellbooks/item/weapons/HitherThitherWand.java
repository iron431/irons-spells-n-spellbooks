package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class HitherThitherWand extends Item implements IPresetSpellContainer {
    public HitherThitherWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("tooltip.irons_spellbooks.spellbook_unique").withStyle(TooltipsUtils.UNIQUE_STYLE));
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            var spellContainer = ISpellContainer.create(1, true, false).mutableCopy();
            spellContainer.addSpell(SkillRegistry.PORTAL_SPELL.get(), 1, true);
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
    }
}