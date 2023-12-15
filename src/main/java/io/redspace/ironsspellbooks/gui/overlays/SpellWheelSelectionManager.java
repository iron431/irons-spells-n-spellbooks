package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SpellWheelSelectionManager {
    public static final String SPELLBOOK_SLOT = "spellbook";
    public static final String MAINHAND = "mainhand";
    public static final String OFFHAND = "offhand";
    private final Player player;
    private final List<SpellItem> spellItemList;
    private SpellWheelSelection spellWheelSelection = null;

    public SpellWheelSelectionManager(Player player) {
        this.player = player;
        this.spellItemList = new ArrayList<>();
        init(player);
    }

    private void init(Player player) {
        var spellbookStack = Utils.getPlayerSpellbookStack(player);
        var mainHandStack = player.getMainHandItem();
        var offHandStack = player.getOffhandItem();

        var tmpSpellWheelSelection = ClientMagicData.getSyncedSpellData(player).getSpellWheelSelection();
        var spellBookData = SpellBookData.getSpellBookData(spellbookStack);
        var mainHandSpellData = SpellData.getSpellData(mainHandStack, false);
        var offHandSpellData = SpellData.getSpellData(offHandStack, false);

        spellBookData.getActiveInscribedSpells().forEach(spellData -> {
            spellItemList.add(new SpellItem(spellData.getSpell(), SPELLBOOK_SLOT));
        });

        if (tmpSpellWheelSelection.equipmentSlot.equals(SPELLBOOK_SLOT) && tmpSpellWheelSelection.index < spellItemList.size()) {
            spellWheelSelection = tmpSpellWheelSelection;
        } else if (tmpSpellWheelSelection.lastEquipmentSlot.equals(SPELLBOOK_SLOT) && tmpSpellWheelSelection.lastIndex < spellItemList.size()) {
            spellWheelSelection = new SpellWheelSelection(tmpSpellWheelSelection.lastEquipmentSlot, tmpSpellWheelSelection.lastIndex);
        }

        if (mainHandSpellData != SpellData.EMPTY) {
            spellItemList.add(new SpellItem(mainHandSpellData.getSpell(), MAINHAND));
            if (spellWheelSelection == null) {
                spellWheelSelection = new SpellWheelSelection(MAINHAND, 0);
            }
        }

        if (offHandSpellData != SpellData.EMPTY) {
            spellItemList.add(new SpellItem(offHandSpellData.getSpell(), OFFHAND));
            if (spellWheelSelection == null) {
                spellWheelSelection = new SpellWheelSelection(OFFHAND, 0);
            }
        }
    }

    public SpellWheelSelection getCurrentSelection() {
        return spellWheelSelection;
    }



    public List<SpellItem> getSpellItems() {
        //TODO: should consider cloning the list instead of returning our copy. deferring to faster for now
        return spellItemList;
    }

    public List<SpellItem> getSpellsForSlot(String slot) {
        return spellItemList.stream().filter(spellItem -> spellItem.slot.equals(slot)).toList();
    }

    public int getSpellCount() {
        return spellItemList.size();
    }

    public static class SpellItem {
        public AbstractSpell spell;
        public String slot;

        public SpellItem(AbstractSpell spell, String slot) {
            this.spell = spell;
            this.slot = slot;
        }
    }
}
