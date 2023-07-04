package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class SpellData {
    public static final String ISB_SPELL = "ISB_spell";
    public static final String LEGACY_SPELL_TYPE = "type";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    private MutableComponent displayName;
    private AbstractSpell spell;
    private final int spellLevel;

    //TODO: remove this after spell reg
    private SpellType legacySpellType;

    private SpellData() throws Exception {
        throw new Exception("Cannot create empty spell data.");
    }

    private SpellData(AbstractSpell spell, int level) {
        this.spell = Objects.requireNonNull(spell);
        this.spellLevel = level;
        this.legacySpellType = spell.getSpellType();
    }

    public int getLegacySpellId() {
        return legacySpellType.getValue();
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

    public static void setSpellData(ItemStack stack, SpellType spellType, int spellLevel) {
        //TODO: remove this once spell reg is complete
        IronsSpellbooks.LOGGER.error("DO NOT USE: setSpellData(ItemStack stack, AbstractSpell spell)");
        var spellTag = new CompoundTag();
        spellTag.putInt(LEGACY_SPELL_TYPE, spellType.getValue());
        spellTag.putInt(SPELL_LEVEL, spellLevel);
        stack.addTagElement(ISB_SPELL, spellTag);
    }

    public static void setSpellData(ItemStack stack, AbstractSpell spell, int spellLevel) {
        setSpellData(stack, spell.getSpellId(), spellLevel);
    }

    public static void setSpellData(ItemStack stack, AbstractSpell spell) {
        //TODO: remove this once spell reg is complete
        IronsSpellbooks.LOGGER.error("DO NOT USE: setSpellData(ItemStack stack, AbstractSpell spell)");
        setSpellData(stack, spell.getSpellId(), 1);
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
            displayName = getSpell().getSpellType().getDisplayName().append(" ").append(Component.translatable(ItemRegistry.SCROLL.get().getDescriptionId()));//.append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity",getSpell().getRarity().getDisplayName().getString()));
        }
        return displayName;
    }
}
