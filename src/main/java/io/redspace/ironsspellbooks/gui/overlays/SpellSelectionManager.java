package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSelectSpell;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpellSelectionManager {
    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();

    private final List<SpellSlot> spellSlotList;
    private SpellSelection spellSelection = null;
    private int selectionIndex = -1;
    private boolean selectionValid = false;
    private final Player player;

    public SpellSelectionManager(@NotNull Player player) {
        this.spellSlotList = new ArrayList<>();
        this.player = player;

        init(player);
    }

    private void init(Player player) {
        if (player.level.isClientSide) {
            spellSelection = ClientMagicData.getSyncedSpellData(player).getSpellSelection();
        } else {
            spellSelection = MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection();
        }

        initSpellbook(player);

        //TODO: support dynamic slot detection
        initItem(player.getMainHandItem(), MAINHAND);
        initItem(player.getOffhandItem(), OFFHAND);

        //Just in case someone wants to mixin to this
        initOther(player);

        if ((selectionIndex == -1 && spellSelection.lastIndex != -1) || (!selectionValid && spellSelection.lastIndex != -1)) {
            tryLastSelection();
        }

        if (!selectionValid && !spellSlotList.isEmpty()) {
            var spellSlot = spellSlotList.get(0);
            setSpellSelection(new SpellSelection(spellSlot.slot, spellSlot.slotIndex));
            selectionIndex = 0;
            selectionValid = true;
        }
    }

    private void initSpellbook(Player player) {
        var spellbookStack = Utils.getPlayerSpellbookStack(player);
        var spellBookData = SpellBookData.getSpellBookData(spellbookStack);
        var activeSpellbookSpells = spellBookData.getActiveInscribedSpells();

        for (int i = 0; i < activeSpellbookSpells.size(); i++) {
            spellSlotList.add(new SpellSlot(activeSpellbookSpells.get(i), Curios.SPELLBOOK_SLOT, i, i));
        }

        if (spellSelection.equipmentSlot.equals(Curios.SPELLBOOK_SLOT) && spellSelection.index < spellSlotList.size()) {
            selectionIndex = spellSelection.index;
            selectionValid = true;
        }
    }

    private void initItem(ItemStack itemStack, String slot) {
        //TODO: expand this to allow an item to have more than 1 spell
        var spellData = SpellData.getSpellData(itemStack, false);
        if (spellData != SpellData.EMPTY) {
            spellSlotList.add(new SpellSlot(spellData, slot, 0, spellSlotList.size()));
            if (spellSelection.equipmentSlot.equals(slot)) {
                selectionIndex = spellSlotList.size() - 1;
                selectionValid = true;
            }
        }
    }

    private void initOther(Player player) {
        //Just in case someone wants to mixin to this
    }

    private void tryLastSelection() {
        if (spellSelection.lastEquipmentSlot.equals(Curios.SPELLBOOK_SLOT) && spellSelection.lastIndex >= 0) {
            var spellbookSpells = getSpellsForSlot(Curios.SPELLBOOK_SLOT);
            if (spellSelection.lastIndex < spellbookSpells.size()) {
                selectionIndex = spellSelection.lastIndex;
                //setSpellSelection(new SpellSelection(Curios.SPELLBOOK_SLOT, spellSelection.lastIndex, spellSelection.equipmentSlot, spellSelection.index));
                selectionValid = true;
            }
        } else if (spellSelection.lastEquipmentSlot.equals(MAINHAND)) {
            var spellSlots = getSpellsForSlot(MAINHAND);
            if (!spellSlots.isEmpty()) {
                //setSpellSelection(new SpellSelection(MAINHAND, 0, spellSelection.equipmentSlot, spellSelection.index));
                selectionIndex = spellSlots.get(0).globalIndex;
                selectionValid = true;
            }
        } else if (spellSelection.lastEquipmentSlot.equals(OFFHAND)) {
            var spellSlots = getSpellsForSlot(OFFHAND);
            if (!spellSlots.isEmpty()) {
                //setSpellSelection(new SpellSelection(OFFHAND, 0, spellSelection.equipmentSlot, spellSelection.index));
                selectionIndex = spellSlots.get(0).globalIndex;
                selectionValid = true;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void makeSelection(int index) {
        if (index != selectionIndex && index >= 0 && index < spellSlotList.size()) {
            var item = spellSlotList.get(index);
            spellSelection.makeSelection(item.slot, item.slotIndex);
            selectionIndex = index;
            selectionValid = true;
            if (player.level.isClientSide) {
                Messages.sendToServer(new ServerboundSelectSpell(spellSelection));
            }
        }
    }

    private void setSpellSelection(SpellSelection spellSelection) {
        IronsSpellbooks.LOGGER.debug("SSM.setSpellSelection old:{} new:{}", this.spellSelection, spellSelection);

        this.spellSelection = spellSelection;
        if (!player.level.isClientSide) {
            MagicData.getPlayerMagicData(player).getSyncedData().setSpellSelection(spellSelection);
        }
    }

    public SpellSelection getCurrentSelection() {
        return spellSelection;
    }

    public SpellSlot getSpellSlot(int index) {
        if (index >= 0 && index < spellSlotList.size()) {
            return spellSlotList.get(index);
        }
        return null;
    }

    public SpellData getSpellData(int index) {
        if (index >= 0 && index < spellSlotList.size()) {
            return spellSlotList.get(index).spellData;
        }
        return SpellData.EMPTY;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    @Nullable
    public SpellSlot getSelectedSpellSlot() {
        if (selectionIndex >= 0 && selectionIndex < spellSlotList.size()) {
            return spellSlotList.get(selectionIndex);
        }
        return null;
    }

    public SpellData getSelectedSpellData() {
        return selectionIndex >= 0 && selectionIndex < spellSlotList.size() ? spellSlotList.get(selectionIndex).spellData : SpellData.EMPTY;
    }

    public List<SpellSlot> getSpellsForSlot(String slot) {
        return spellSlotList.stream().filter(spellSlot -> spellSlot.slot.equals(slot)).toList();
    }

    public List<SpellSlot> getAllSpells() {
        return spellSlotList;
    }

    public SpellData getSpellForSlot(String slot, int index) {
        var spells = getSpellsForSlot(slot);

        if (index >= 0 && index < spells.size()) {
            return spells.get(index).spellData;
        }

        return SpellData.EMPTY;
    }

    public int getSpellCount() {
        return spellSlotList.size();
    }

    public static class SpellSlot {
        public SpellData spellData;
        public String slot;
        public int slotIndex;
        public int globalIndex;

        public SpellSlot(SpellData spell, String slot, int slotIndex, int globalIndex) {
            this.spellData = spell;
            this.slot = slot;
            this.slotIndex = slotIndex;
            this.globalIndex = globalIndex;
        }

        public CastSource getCastSource() {
            return this.slot.equals(Curios.SPELLBOOK_SLOT) ? CastSource.SPELLBOOK : CastSource.SWORD;
        }
    }
}
