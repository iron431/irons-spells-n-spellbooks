package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SpellWheelSelectionManager {
    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();

    private final List<SpellItem> spellItemList;
    private SpellWheelSelection spellWheelSelection = null;
    private int selectionIndex = -1;

    public SpellWheelSelectionManager(Player player) {
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
            spellItemList.add(new SpellItem(spellData, Curios.SPELLBOOK_SLOT));
        });

        if (tmpSpellWheelSelection.equipmentSlot.equals(Curios.SPELLBOOK_SLOT) && tmpSpellWheelSelection.index < spellItemList.size()) {
            spellWheelSelection = tmpSpellWheelSelection;
            selectionIndex = tmpSpellWheelSelection.index;
        } else if (tmpSpellWheelSelection.lastEquipmentSlot.equals(Curios.SPELLBOOK_SLOT) && tmpSpellWheelSelection.lastIndex < spellItemList.size()) {
            spellWheelSelection = new SpellWheelSelection(tmpSpellWheelSelection.lastEquipmentSlot, tmpSpellWheelSelection.lastIndex);
            selectionIndex = tmpSpellWheelSelection.lastIndex;
            ;
        }

        if (mainHandSpellData != SpellData.EMPTY) {
            spellItemList.add(new SpellItem(mainHandSpellData, MAINHAND));
            if (spellWheelSelection == null) {
                spellWheelSelection = new SpellWheelSelection(MAINHAND, 0);
                selectionIndex = spellItemList.size() + 1;
            }
        }

        if (offHandSpellData != SpellData.EMPTY) {
            spellItemList.add(new SpellItem(offHandSpellData, OFFHAND));
            if (spellWheelSelection == null) {
                spellWheelSelection = new SpellWheelSelection(OFFHAND, 0);
                selectionIndex = spellItemList.size() + 2;
            }
        }

        if (spellWheelSelection == null) {
            selectionIndex = -1;
            spellWheelSelection = new SpellWheelSelection();
        }
    }

    public SpellWheelSelection getCurrentSelection() {
        return spellWheelSelection;
    }

    public SpellData getSpellData(int index){
        return spellItemList.get(index).spellData;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public SpellItem getSelectedSpellItem() {
        return spellItemList.get(selectionIndex);
    }

    public SpellData getSelectedSpellData() {
        return spellItemList.get(selectionIndex).spellData;
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
        public SpellData spellData;
        public String slot;

        public SpellItem(SpellData spell, String slot) {
            this.spellData = spell;
            this.slot = slot;
        }
    }
}
