package io.redspace.ironsspellbooks.item.spell_containers;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.data.SkillSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ScrollContainer{

    public static boolean has(ItemStack stack) {
        return stack.has(ComponentRegistry.SCROLL_CONTAINER);
    }

    public static ISkillContainer get(ItemStack stack) {
        return stack.get(ComponentRegistry.SCROLL_CONTAINER);
    }

    public static void set(ItemStack stack, ISkillContainer container) {
        stack.set(ComponentRegistry.SCROLL_CONTAINER, container);
    }

    public static void set(ItemStack stack, AbstractSpell spell, int level) {
        set(stack, create(new SkillData(spell, level)));
    }

    public static ISkillContainer create(SkillData skillData) {
        return new SkillContainer(1, false, false, new SkillSlot[]{new SkillSlot(skillData, 0)});
    }

    public static @Nullable SkillData getScrollData(ItemStack itemStack) {
        if (!has(itemStack)) {
            return null;
        }
        var container = get(itemStack);
        if (container.isEmpty()) {
            return null;
        }
        return container.getSkillAtIndex(0);
    }
}
