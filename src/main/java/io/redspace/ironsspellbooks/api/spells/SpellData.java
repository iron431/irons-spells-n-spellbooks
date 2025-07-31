package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public class SpellData implements Comparable<SpellData> {
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final String SPELL_LOCKED = "locked";
    public static final SpellData EMPTY = new SpellData(SpellRegistry.none(), 0, false);
    private MutableComponent displayName;
    protected final AbstractSpell spell;
    protected final int spellLevel;
    protected final boolean locked;

    private SpellData() throws Exception {
        throw new Exception("Cannot create empty spell slots.");
    }

    public SpellData(AbstractSpell spell, int level, boolean locked) {
        this.spell = Objects.requireNonNull(spell);
        this.spellLevel = level;
        this.locked = locked;
    }

    public SpellData(AbstractSpell spell, int level) {
        this(spell, level, false);
    }

//    public static SpellSlot getSpellData(ItemStack stack) {
//        return getSpellData(stack, true);
//    }
//
//    public static SpellSlot getSpellData(ItemStack stack, boolean includeScrolls) {
//        CompoundTag tag = stack.getTagElement(ISB_SPELL);
//
//        if (!includeScrolls && stack.is(ItemRegistry.SCROLL.get())) {
//            return EMPTY;
//        }
//
//        if (tag != null) {
//            return new SpellSlot(SpellRegistry.getSpell(ResourceLocation.fromNamespaceAndPath(tag.getString(SPELL_ID))), tag.getInt(SPELL_LEVEL));
//        } else if (stack.getItem() instanceof MagicSwordItem magicSwordItem) {
//            var spell = magicSwordItem.getImbuedSpell();
//            setSpellData(stack, spell, magicSwordItem.getImbuedLevel());
//            return new SpellSlot(spell, magicSwordItem.getImbuedLevel());
//        } else {
//            return EMPTY;
//        }
//    }

    public AbstractSpell getSpell() {
        return spell == null ? SpellRegistry.none() : spell;
    }

    public int getLevel() {
        return spellLevel;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean canRemove() {
        return !locked;
    }

    public SpellRarity getRarity() {
        return getSpell().getRarity(getLevel());
    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = getSpell().getDisplayName(MinecraftInstanceHelper.instance.player()).append(" ").append(Component.translatable(ItemRegistry.SCROLL.get().getDescriptionId()));
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
