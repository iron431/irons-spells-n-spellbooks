package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public interface ISpellContainer {
    @NotNull SpellSlot[] getAllSpells();

    @NotNull List<SpellSlot> getActiveSpells();

    int getMaxSpellCount();


    int getActiveSpellCount();

    int getNextAvailableIndex();

    boolean mustEquip();

    boolean isImproved();

    boolean isSpellWheel();

    @NotNull SpellData getSpellAtIndex(int index);

    int getIndexForSpell(AbstractSpell spell);

    boolean isEmpty();

    ISpellContainerMutable mutableCopy();

    /*
     * Static Helpers
     */
    static boolean isSpellContainer(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && CodecHelper.has(itemStack, NBT);
    }

    static ISpellContainer create(int maxSpells, boolean addsToSpellWheel, boolean mustBeEquipped) {
        return new SpellContainer(maxSpells, addsToSpellWheel, mustBeEquipped);
    }

    static ISpellContainer createScrollContainer(AbstractSpell spell, int spellLevel, ItemStack itemStack) {
        var spellContainer = create(1, false, false).mutableCopy();
        spellContainer.addSpellAtIndex(spell, spellLevel, 0, true);
        var i = spellContainer.toImmutable();
        ISpellContainer.set(itemStack, i);
        return i;
    }

    static ISpellContainer createImbuedContainer(AbstractSpell spell, int spellLevel, ItemStack itemStack) {
        var spellContainer = create(1, true, (itemStack.getItem() instanceof ArmorItem || itemStack.getItem() instanceof ICurioItem)).mutableCopy();
        spellContainer.addSpellAtIndex(spell, spellLevel, 0, true);
        var i = spellContainer.toImmutable();
        ISpellContainer.set(itemStack, i);
        return i;
    }

    static ISpellContainer get(ItemStack itemStack) {
        return CodecHelper.get(SpellContainer.CODEC, itemStack.getOrCreateTag().getCompound(NBT));
//        return itemStack.get(ComponentRegistry.SPELL_CONTAINER);
    }

    static ISpellContainer getOrCreate(ItemStack itemStack) {
//        return itemStack.getOrDefault(ComponentRegistry.SPELL_CONTAINER, new SpellContainer(1, true, false));
        if (isSpellContainer(itemStack)) {
            return get(itemStack);
        } else {
            return new SpellContainer(1, true, false);
        }
    }

    static void set(ItemStack stack, ISpellContainer container) {
//        stack.set(ComponentRegistry.SPELL_CONTAINER, container);
        CodecHelper.set(stack, NBT, SpellContainer.CODEC, container);
    }

    static void remove(ItemStack stack) {
        stack.removeTagKey(NBT);
    }

    static final String NBT = "irons_spellbooks:spell_container";
}
