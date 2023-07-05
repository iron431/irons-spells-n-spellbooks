package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
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

            if (tag.contains(LEGACY_SPELL_TYPE)) {
                IronsSpellbooks.LOGGER.debug("Legacy spell type found: {}", tag.getInt(LEGACY_SPELL_TYPE));
                //TODO: deal with this when spell type, level and registration are further along.. or deal with en mass when the world is loading?
            }

            return new SpellData(SpellRegistry.getSpell(new ResourceLocation(tag.getString(SPELL_ID))), tag.getInt(SPELL_LEVEL));
        } else if (stack.getItem() instanceof ExtendedSwordItem extendedSwordItem) {
            var spell = SpellRegistry.getSpell(new ResourceLocation(extendedSwordItem.getImbuedSpellId()));
            setSpellData(stack, spell, extendedSwordItem.getImbuedLevel());
            return new SpellData(spell, extendedSwordItem.getImbuedLevel());
        } else {
            return new SpellData(SpellRegistry.none(), 0);
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

    public static void setSpellData(ItemStack stack, int spellType, int spellLevel) {
        var spellTag = new CompoundTag();
        spellTag.putInt(LEGACY_SPELL_TYPE, spellType);
        spellTag.putInt(SPELL_LEVEL, spellLevel);
        stack.addTagElement(ISB_SPELL, spellTag);
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
            displayName = getSpell().getDisplayName().append(" ").append(Component.translatable(ItemRegistry.SCROLL.get().getDescriptionId()));//.append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity",getSpell().getRarity().getDisplayName().getString()));
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
        int i = this.spell.getSpellResource().compareTo(other.spell.getSpellResource());
        if (i == 0) {
            i = Integer.compare(this.spellLevel, other.spellLevel);
        }
        return i;
    }
}
