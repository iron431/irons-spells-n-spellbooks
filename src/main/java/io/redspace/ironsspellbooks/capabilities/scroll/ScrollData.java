package io.redspace.ironsspellbooks.capabilities.scroll;

import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class ScrollData {

    public static final String SPELL_ID = "spellId";
    public static final String LEVEL = "level";
    private MutableComponent displayName;
    private List<MutableComponent> hoverText;
    private AbstractSpell spell;
    private int spellId;
    private int spellLevel;

    public ScrollData(SpellType spellType, int level) {
        this.spellId = spellType.getValue();
        this.spellLevel = level;
        //irons_spellbooks.LOGGER.debug("ScrollData.1: {}, {}", spellId, spellLevel);
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

//    public void setData(int spellId, int spellLevel) {
//        this.spellId = spellId;
//        this.spellLevel = spellLevel;
//    }
//
//    public void setData(AbstractSpell spell) {
//        this.spellId = spell.getID();
//        this.spellLevel = spell.getLevel();
//    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = getSpell().getSpellType().getDisplayName().append(" ").append(Component.translatable("item.irons_spellbooks.scroll"));//.append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity",getSpell().getRarity().getDisplayName().getString()));
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

    public CompoundTag saveNBTData() {
        //irons_spellbooks.LOGGER.debug("ScrollData.saveNBTData: {} {}", spellId, spellLevel);
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_ID, spellId);
        compound.putInt(LEVEL, spellLevel);
        return (compound);
    }

    public void loadNBTData(CompoundTag compound) {
        //irons_spellbooks.LOGGER.debug("ScrollData.loadNBTData: {}", compound);
        spellId = compound.getInt(SPELL_ID);
        spellLevel = compound.getInt(LEVEL);
    }
}
