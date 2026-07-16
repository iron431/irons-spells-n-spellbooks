package io.redspace.ironsspellbooks.item.spell_containers;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import net.minecraft.world.item.ItemStack;

public class SpellbookContainer {

    public static boolean has(ItemStack stack) {
        return stack.has(ComponentRegistry.SPELLBOOK_CONTAINER);
    }

    public static ISkillContainer get(ItemStack stack) {
        return stack.get(ComponentRegistry.SPELLBOOK_CONTAINER);
    }

    public static void set(ItemStack stack, ISkillContainer container) {
        stack.set(ComponentRegistry.SPELLBOOK_CONTAINER, container);
    }
}
