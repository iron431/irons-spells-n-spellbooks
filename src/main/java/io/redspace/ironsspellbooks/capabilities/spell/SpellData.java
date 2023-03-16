package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SpellData {

    public static final String SPELL_ID = "spellId";
    public static final String LEVEL = "level";
    public static final String ISB_SPELL = "ISB_spell";
    public static final String SPELL_TYPE = "type";
    public static final String SPELL_LEVEL = "level";
    private MutableComponent displayName;
    private List<MutableComponent> hoverText;
    private AbstractSpell spell;
    private int spellId;
    private int spellLevel;

    public SpellData(SpellType spellType, int level) {
        this.spellId = spellType.getValue();
        this.spellLevel = level;
        //irons_spellbooks.LOGGER.debug("ScrollData.1: {}, {}", spellId, spellLevel);
    }

    public static SpellData getSpellData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(ISB_SPELL);

        if (tag != null) {
            return new SpellData(SpellType.getTypeFromValue(tag.getInt(SPELL_TYPE)), tag.getInt(SPELL_LEVEL));
        } else {
            return new SpellData(SpellType.NONE_SPELL, 0);
        }
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
        setSpellData(stack, spell.getSpellType().getValue(), spell.getLevel());
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

    public List<MutableComponent> getHoverText() {
        if (hoverText == null) {
            hoverText = Lists.newArrayList();
            var spell = getSpell();
            if (spell.getSpellType() != SpellType.NONE_SPELL) {
                //hoverText.add(s.getRarity().getDisplayName().copy());
                //hoverText.add(Component.translatable("tooltip.irons_spellbooks.level", s.getLevel()).withStyle(ChatFormatting.GRAY));
                hoverText.add(Component.translatable("tooltip.irons_spellbooks.level", spell.getLevel()).append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity", getSpell().getRarity().getDisplayName().getString())).withStyle(getSpell().getRarity().getDisplayName().getStyle()));
                for (MutableComponent component : spell.getUniqueInfo())
                    hoverText.add(component.withStyle(ChatFormatting.GRAY));
                if (spell.getCastType() != CastType.INSTANT) {
                    String castKey = spell.getCastType() == CastType.CONTINUOUS ? "tooltip.irons_spellbooks.cast_continuous" : "tooltip.irons_spellbooks.cast_long";
                    hoverText.add(Component.translatable(castKey, Utils.timeFromTicks(spell.getCastTime(), 1)).withStyle(ChatFormatting.GRAY));
                }
                hoverText.add(Component.empty());
                hoverText.add(Component.translatable("tooltip.irons_spellbooks.scroll_tooltip").withStyle(ChatFormatting.GRAY));
                hoverText.add(Component.translatable("tooltip.irons_spellbooks.mana_cost", spell.getManaCost()).withStyle(ChatFormatting.BLUE));
                hoverText.add(Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(spell.getSpellCooldown(), 1)).withStyle(ChatFormatting.BLUE));
                hoverText.add(spell.getSchoolType().getDisplayName().copy());
            }

        }
        return hoverText;
    }
}
