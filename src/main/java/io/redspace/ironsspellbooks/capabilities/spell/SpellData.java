package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class SpellData implements Comparable<SpellData> {
    public static final String ISB_SPELL = "ISB_spell";
    public static final String LEGACY_SPELL_TYPE = "type";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final SpellData EMPTY = new SpellData(SpellRegistry.none(), 0);
    private MutableComponent displayName;
    private final AbstractSpell spell;
    private final int spellLevel;

    private SpellData() throws Exception {
        throw new Exception("Cannot create empty spell data.");
    }

    public SpellData(AbstractSpell spell, int level) {
        this.spell = Objects.requireNonNull(spell);
        this.spellLevel = level;
    }

    public static SpellData getSpellData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(ISB_SPELL);

        if (tag != null) {
//            if (tag.contains(LEGACY_SPELL_TYPE)) {
//                DataFixerHelpers.fixScrollData(tag);
//            }

            return new SpellData(SpellRegistry.getSpell(new ResourceLocation(tag.getString(SPELL_ID))), tag.getInt(SPELL_LEVEL));
        } else if (stack.getItem() instanceof MagicSwordItem magicSwordItem) {
            var spell = magicSwordItem.getImbuedSpell();
            setSpellData(stack, spell, magicSwordItem.getImbuedLevel());
            return new SpellData(spell, magicSwordItem.getImbuedLevel());
        } else {
            return EMPTY;
        }
    }

    public static boolean hasSpellData(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement(ISB_SPELL);
        return tag != null;
    }

    public static void setSpellData(ItemStack stack, String spellId, int spellLevel) {
        var spellTag = new CompoundTag();
        spellTag.putString(SPELL_ID, spellId);
        spellTag.putInt(SPELL_LEVEL, spellLevel);
        stack.addTagElement(ISB_SPELL, spellTag);
    }

    public static void setSpellData(ItemStack stack, SpellData spellData) {
        setSpellData(stack, spellData.getSpell().getSpellId(), spellData.getLevel());
    }

    public static void setSpellData(ItemStack stack, AbstractSpell spell, int spellLevel) {
        setSpellData(stack, spell.getSpellId(), spellLevel);
    }

    public AbstractSpell getSpell() {
        if (spell == null) {
            return SpellRegistry.none();
        }
        return spell;
    }

    public int getLevel() {
        return spellLevel;
    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = getSpell().getDisplayName().append(" ").append(Component.translatable(ItemRegistry.SCROLL.get().getDescriptionId()));
        }
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof SpellData other) {
            return this.spell.equals(other.spell) && this.spellLevel == other.spellLevel;
        }

        return false;
    }

    public int hashCode() {
        return 31 * this.spell.hashCode() + this.spellLevel;
    }

    public int compareTo(SpellData other) {
        int i = this.spell.getSpellId().compareTo(other.spell.getSpellId());
        if (i == 0) {
            i = Integer.compare(this.spellLevel, other.spellLevel);
        }
        return i;
    }
}
