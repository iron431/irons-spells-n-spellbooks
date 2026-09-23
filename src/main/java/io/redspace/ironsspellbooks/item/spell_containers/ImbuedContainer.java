package io.redspace.ironsspellbooks.item.spell_containers;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import net.minecraft.world.item.ItemStack;

// todo: can simplify that imbued is always spell wheel
public class ImbuedContainer {
    public static boolean has(ItemStack stack) {
        Utils.resolveLegacySpellContainer(stack);
        return stack.has(ComponentRegistry.IMBUED_SPELL_CONTAINER);
    }

    public static ISkillContainer get(ItemStack stack) {
        Utils.resolveLegacySpellContainer(stack);
        return stack.get(ComponentRegistry.IMBUED_SPELL_CONTAINER);
    }

    public static void set(ItemStack stack, ISkillContainer container) {
        stack.set(ComponentRegistry.IMBUED_SPELL_CONTAINER, container);
    }

    public static ISkillContainer create(int maxSize, boolean mustEquip) {
        return ISkillContainer.create(mustEquip, maxSize);
    }

    public static void applyImbue(ItemStack stack, AbstractSpell spell, int level) {
        set(stack, ISkillContainer.create(false, new SkillData(spell, level)));
    }
}
