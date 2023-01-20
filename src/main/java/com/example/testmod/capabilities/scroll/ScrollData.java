package com.example.testmod.capabilities.scroll;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
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
        //TestMod.LOGGER.debug("ScrollData.1: {}, {}", spellId, spellLevel);
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

    public void setData(int spellId, int spellLevel) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
    }
    public void setData(AbstractSpell spell) {
        this.spellId = spell.getID();
        this.spellLevel = spell.getLevel();
    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = getSpell().getSpellType().getDisplayName().append(" ").append(Component.translatable("item.testmod.scroll"));//.append(" ").append(Component.translatable("tooltip.testmod.rarity",getSpell().getRarity().getDisplayName().getString()));
        }
        return displayName;
    }

    public List<MutableComponent> getHoverText() {
        if (hoverText == null) {
            hoverText = Lists.newArrayList();
            var s = getSpell();
            if (s.getSpellType() != SpellType.NONE_SPELL) {
                //hoverText.add(s.getRarity().getDisplayName().copy());
                //hoverText.add(Component.translatable("tooltip.testmod.level", s.getLevel()).withStyle(ChatFormatting.GRAY));
                hoverText.add(Component.translatable("tooltip.testmod.level",s.getLevel()).append(" ").append(Component.translatable("tooltip.testmod.rarity",getSpell().getRarity().getDisplayName().getString())).withStyle(getSpell().getRarity().getDisplayName().getStyle()));
                if (s.getUniqueInfo() != null)
                    hoverText.add(s.getUniqueInfo().withStyle(ChatFormatting.GRAY));
                hoverText.add(Component.empty());
                hoverText.add(Component.translatable("tooltip.testmod.scroll_tooltip").withStyle(ChatFormatting.GRAY));
                hoverText.add(Component.translatable("tooltip.testmod.mana_cost", s.getManaCost()).withStyle(ChatFormatting.BLUE));
                hoverText.add(Component.translatable("tooltip.testmod.cooldown_length_seconds", Utils.timeFromTicks(s.getSpellCooldown(), 1)).withStyle(ChatFormatting.BLUE));
                hoverText.add(s.getSchoolType().getDisplayName().copy());
            }

        }
        return hoverText;
    }

    public CompoundTag saveNBTData() {
        //TestMod.LOGGER.debug("ScrollData.saveNBTData: {} {}", spellId, spellLevel);
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_ID, spellId);
        compound.putInt(LEVEL, spellLevel);
        return (compound);
    }

    public void loadNBTData(CompoundTag compound) {
        //TestMod.LOGGER.debug("ScrollData.loadNBTData: {}", compound);
        spellId = compound.getInt(SPELL_ID);
        spellLevel = compound.getInt(LEVEL);
    }
}
