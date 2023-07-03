package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SpellData {

    public static final String ISB_SPELL = "ISB_spell";
    public static final String SPELL_TYPE = "type";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    private MutableComponent displayName;
    private List<MutableComponent> hoverText;
    private AbstractSpell spell;
    private final int spellId;
    private final int spellLevel;

    public SpellData(SpellType spellType, int level) {
        this.spellId = spellType.getValue();
        this.spellLevel = level;
    }

    public static SpellData getSpellData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(ISB_SPELL);

        if (tag != null) {
            if (tag.contains(SPELL_TYPE)) {
                IronsSpellbooks.LOGGER.debug("Legacy spell id found.. converting");
                //TODO: deal with this when spelltype, level and registration are further along
            }

            return new SpellData(SpellType.getTypeFromValue(tag.getInt(SPELL_TYPE)), tag.getInt(SPELL_LEVEL));
        } else if (stack.getItem() instanceof ExtendedSwordItem extendedSwordItem) {
            setSpellData(stack, extendedSwordItem.getImbuedSpell(), extendedSwordItem.getImbuedLevel());
            return new SpellData(extendedSwordItem.getImbuedSpell(), extendedSwordItem.getImbuedLevel());
        } else {
            return new SpellData(SpellType.NONE_SPELL, 0);
        }
    }

    public static boolean hasSpellData(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement(ISB_SPELL);
        return tag != null;
    }

    public static void setSpellData(ItemStack stack, int spellTypeId, int spellLevel) {
        var spellTag = new CompoundTag();
        spellTag.putInt(SPELL_TYPE, spellTypeId);
        spellTag.putInt(SPELL_LEVEL, spellLevel);
        stack.addTagElement(ISB_SPELL, spellTag);
    }

    public static void setSpellData(ItemStack stack, SpellType spellType, int spellLevel) {
        setSpellData(stack, spellType.getValue(), spellLevel);
    }

    public static void setSpellData(ItemStack stack, AbstractSpell spell) {
        setSpellData(stack, spell.getSpellType().getValue(), spell.getLevel(null));
    }

    public AbstractSpell getSpell() {
        if (spell == null) {
            spell = AbstractSpell.getSpell(spellId, spellLevel);
        }

        return spell;
    }

    public int getSpellId() {
        return spellId;
    }

    public int getLevel() {
        return spellLevel;
    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = getSpell().getSpellType().getDisplayName().append(" ").append(Component.translatable(ItemRegistry.SCROLL.get().getDescriptionId()));//.append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity",getSpell().getRarity().getDisplayName().getString()));
        }
        return displayName;
    }
}
