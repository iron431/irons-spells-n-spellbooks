package com.example.testmod.capabilities.scroll.data;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ScrollData {

    public static final String SPELL_ID = "spellId";
    public static final String LEVEL = "level";
    private AbstractSpell spell;
    private Component displayName;
    private List<Component> hoverText;

    private CompoundTag tag = new CompoundTag();

    public ScrollData(SpellType spellType, int level) {
        this.spell = AbstractSpell.getSpell(spellType, level);
        this.tag = saveNBTData();
    }

    public ScrollData(CompoundTag compound) {
        loadNBTData(compound);
    }

    public AbstractSpell getSpell() {
        return this.spell;
    }

    public CompoundTag saveNBTData() {
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_ID, this.spell.getID());
        compound.putInt(LEVEL, this.spell.getLevel());
        return (compound);
    }

    public Component getDisplayName() {
        if (displayName == null) {
            displayName = spell.getSpellType().getDisplayName().append(" ").append(new TranslatableComponent("item.testmod.scroll"));
        }
        return displayName;
    }

    public List<Component> getHoverText() {
        if (hoverText == null) {
            hoverText = Lists.newArrayList();
            hoverText.add(new TranslatableComponent("tooltip.testmod.level",spell.getLevel()).withStyle(ChatFormatting.GRAY));
            hoverText.add(TextComponent.EMPTY);
            hoverText.add(new TranslatableComponent("tooltip.testmod.scroll_tooltip").withStyle(ChatFormatting.GRAY));
            hoverText.add(new TranslatableComponent("tooltip.testmod.mana_cost",spell.getManaCost()).withStyle(ChatFormatting.BLUE));
            hoverText.add(new TranslatableComponent("tooltip.testmod.cooldown_length_seconds", Utils.TimeFromTicks(getSpell().getSpellCooldown(), 1)).withStyle(ChatFormatting.BLUE));
            //hoverText.add(new TranslatableComponent("ui.testmod.cast_type","Instant").withStyle(ChatFormatting.DARK_GREEN));
        }
        return hoverText;
    }

    public void loadNBTData(CompoundTag compound) {
        int spellId = compound.getInt(SPELL_ID);
        int spellLevel = compound.getInt(LEVEL);
        this.spell = AbstractSpell.getSpell(spellId, spellLevel);
        this.tag = saveNBTData();
    }
}
