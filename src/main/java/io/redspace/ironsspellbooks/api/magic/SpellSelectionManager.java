package io.redspace.ironsspellbooks.api.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSelectSpell;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpellSelectionManager {
    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();
    /**
     * List of selectable spells -- the spell wheel
     */
    private final List<SelectionOption> selectionOptionList;
    /**
     * Current Selected Spell
     */
    private SpellSelection spellSelection = null;
    /**
     * Global index of the current selection (spellSelection) from selectionOptionList
     */
    private int selectionIndex = -1;
    private boolean selectionValid = false;
    private final Player player;

    public SpellSelectionManager(@NotNull Player player) {
        this.selectionOptionList = new ArrayList<>();
        this.player = player;
        init(player);
    }

    private void init(Player player) {
        if (player == null) return;

        if (player.level.isClientSide) {
            spellSelection = ClientMagicData.getSyncedSpellData(player).getSpellSelection();
        } else {
            spellSelection = MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection();
        }

        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("SpellSelectionManager init.begin spellSelection:{} valid:{} index:{} isClient:{}", spellSelection, selectionValid, selectionIndex, player.level.isClientSide);
        }

        //TODO: support dynamic slot detection for curios
        initItem(Utils.getPlayerSpellbookStack(player), Curios.SPELLBOOK_SLOT);
        initItem(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD.getName());
        initItem(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST.getName());
        initItem(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS.getName());
        initItem(player.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET.getName());
        initItem(player.getItemBySlot(EquipmentSlot.MAINHAND), MAINHAND);
        initItem(player.getItemBySlot(EquipmentSlot.OFFHAND), OFFHAND);
        MinecraftForge.EVENT_BUS.post(new SpellSelectionEvent(this.player, this));

        if (!selectionValid && !selectionOptionList.isEmpty()) {
            tryLastSelectionOrDefault();
        }

        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("SpellSelectionManager init.end spellSelection:{} valid:{} index:{} isClient:{}", spellSelection, selectionValid, selectionIndex, player.level.isClientSide);
        }
    }

    private void initItem(@Nullable ItemStack itemStack, String equipmentSlot) {
        if (ISpellContainer.isSpellContainer(itemStack)) {
            var spellContainer = ISpellContainer.get(itemStack);
            if (spellContainer.spellWheel() && (!spellContainer.mustEquip() || (!equipmentSlot.equals(MAINHAND) && !equipmentSlot.equals(OFFHAND)))) {
                var activeSpells = spellContainer.getActiveSpells();
                for (int i = 0; i < activeSpells.size(); i++) {
                    var spellSlot = activeSpells.get(i);
                    selectionOptionList.add(new SelectionOption(spellSlot, equipmentSlot, i, selectionOptionList.size()));

                    if (spellSelection.index == i && spellSelection.equipmentSlot.equals(equipmentSlot)) {
                        selectionIndex = selectionOptionList.size() - 1;
                        selectionValid = true;
                    }
                }
            }
        }
    }

    private void tryLastSelectionOrDefault() {
//        if (!selectionOptionList.isEmpty() && ((selectionIndex == -1 && spellSelection.lastIndex != -1) || (!selectionValid && spellSelection.lastIndex != -1))) {
//            tryLastSelection();
//        }


        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("SpellSelectionManager tryLastSelection");
        }

        if (spellSelection.lastEquipmentSlot.isEmpty()) {
            var select = selectionOptionList.stream().findFirst();
            select.ifPresent(selection -> {
                makeLocalSelection(selection.slot, selection.slotIndex, selection.globalIndex, false);
            });
        } else if (spellSelection.lastIndex != -1) {
            var spellsForSlot = getSpellsForSlot(spellSelection.lastEquipmentSlot);
            if (!spellsForSlot.isEmpty()) {
                if (spellSelection.lastIndex < spellsForSlot.size()) {
                    makeLocalSelection(spellSelection.lastEquipmentSlot, spellSelection.lastIndex, spellsForSlot.get(spellSelection.lastIndex).globalIndex, false);
                } else {
                    makeLocalSelection(spellSelection.lastEquipmentSlot, 0, spellsForSlot.get(0).globalIndex, false);
                }
            }
        } else {
            if (Log.SPELL_SELECTION) {
                IronsSpellbooks.LOGGER.warn("SpellSelectionManager invalid spellSelection:{}", spellSelection);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void makeSelection(int index) {
        if (index != selectionIndex && index >= 0 && index < selectionOptionList.size()) {
            var item = selectionOptionList.get(index);
            makeLocalSelection(item.slot, item.slotIndex, index, true);
        }
    }

    private void makeLocalSelection(String slot, int slotIndex, int globalIndex, boolean doSync) {
        selectionIndex = globalIndex;
        selectionValid = true;
        if (doSync && player.level.isClientSide) {
            spellSelection.makeSelection(slot, slotIndex);
            Messages.sendToServer(new ServerboundSelectSpell(spellSelection));
        }
    }

    private void setSpellSelection(SpellSelection spellSelection) {
        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("SSM.setSpellSelection old:{} new:{}", this.spellSelection, spellSelection);
        }

        this.spellSelection = spellSelection;
        if (player.level.isClientSide) {
            Messages.sendToServer(new ServerboundSelectSpell(spellSelection));
        } else {
            MagicData.getPlayerMagicData(player).getSyncedData().setSpellSelection(spellSelection);
        }
    }

    public SpellSelection getCurrentSelection() {
        return spellSelection;
    }

    public SelectionOption getSpellSlot(int index) {
        if (index >= 0 && index < selectionOptionList.size()) {
            return selectionOptionList.get(index);
        }
        return null;
    }

    public SpellData getSpellData(int index) {
        if (index >= 0 && index < selectionOptionList.size()) {
            return selectionOptionList.get(index).spellData;
        }
        return SpellData.EMPTY;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public int getGlobalSelectionIndex() {
        var selection = getSelection();

        if (selection == null) {
            return -1;
        }

        return getSelection().globalIndex;
    }

    @Nullable
    public SpellSelectionManager.SelectionOption getSelection() {
        if (selectionIndex >= 0 && selectionIndex < selectionOptionList.size()) {
            return selectionOptionList.get(selectionIndex);
        } else if (!selectionOptionList.isEmpty()) {
            return selectionOptionList.get(0);
        }
        return null;
    }

    public SpellData getSelectedSpellData() {
        return selectionIndex >= 0 && selectionIndex < selectionOptionList.size() ? selectionOptionList.get(selectionIndex).spellData : SpellData.EMPTY;
    }

    public List<SelectionOption> getSpellsForSlot(String slot) {
        return selectionOptionList.stream().filter(selectionOption -> selectionOption.slot.equals(slot)).toList();
    }

    public List<SelectionOption> getAllSpells() {
        return selectionOptionList;
    }

    public SpellData getSpellForSlot(String slot, int index) {
        var spells = getSpellsForSlot(slot);

        if (index >= 0 && index < spells.size()) {
            return spells.get(index).spellData;
        }

        return SpellData.EMPTY;
    }

    public int getSpellCount() {
        return selectionOptionList.size();
    }

    public static class SelectionOption {
        public SpellData spellData;
        public String slot;
        public int slotIndex;
        public int globalIndex;

        public SelectionOption(SpellData spellData, String slot, int slotIndex, int globalIndex) {
            this.spellData = spellData;
            this.slot = slot;
            this.slotIndex = slotIndex;
            this.globalIndex = globalIndex;
        }

        public CastSource getCastSource() {
            return this.slot.equals(Curios.SPELLBOOK_SLOT) ? CastSource.SPELLBOOK : CastSource.SWORD;
        }
    }

    /**
     * SpellSelectionEvent is fired on the client and server whenever a {@link Player}'s spell selection manager is initialized ({@link SpellSelectionManager#init(Player)}), after their equipment items have been processed.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link Event.HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     **/
    public static class SpellSelectionEvent extends PlayerEvent {
        SpellSelectionManager manager;

        public SpellSelectionEvent(Player player, SpellSelectionManager manager) {
            super(player);
            this.manager = manager;
        }

        /**
         * Adds spell option to the end of a player's spell bar
         *
         * @param spellData      Spell
         * @param slotId         Slot identifier, ie "chestplate"
         * @param localSlotIndex Index of the slot to other like-types, ie helmet = 0, chestplate = 1, etc
         */
        public void addSelectionOption(SpellData spellData, String slotId, int localSlotIndex) {
            addSelectionOption(spellData, slotId, localSlotIndex, manager.selectionOptionList.size());
        }

        /**
         * Adds spell option to the specified location in a player's spell bar
         *
         * @param spellData      Spell
         * @param slotId         Slot identifier, ie "chestplate"
         * @param localSlotIndex Index of the slot with regard to other like-types, ie helmet = 0, chestplate = 1... etc
         * @param globalIndex    Index of the spell within the manager (where it will appear on the spell bar)
         */
        public void addSelectionOption(SpellData spellData, String slotId, int localSlotIndex, int globalIndex) {
            //FIXME: spell selection manager does not work the way i remember it, injecting into specific slot indexes currently doesn't work
            globalIndex = manager.selectionOptionList.size();
            if (globalIndex >= 0 && globalIndex <= manager.selectionOptionList.size()) {
                manager.selectionOptionList.add(globalIndex, new SelectionOption(spellData, slotId, localSlotIndex, globalIndex));
//                if (globalIndex != manager.selectionOptionList.size()) {
//                    for (int i = globalIndex + 1; i < manager.selectionOptionList.size(); i++) {
//                        manager.selectionOptionList.get(i).globalIndex++;
//                    }
//                }
                if (manager.spellSelection.index == localSlotIndex && manager.spellSelection.equipmentSlot.equals(slotId)) {
                    manager.selectionIndex = manager.selectionOptionList.size() - 1;
                    manager.selectionValid = true;
                }
            }
        }

        public SpellSelectionManager getManager() {
            return manager;
        }

        @Override
        public boolean isCancelable() {
            return false;
        }
    }
}
