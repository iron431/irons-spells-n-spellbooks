package io.redspace.skillcasting.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.network.SelectSkillPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class SkillSelectionManager {

    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();

    private static final Codec<SelectionOption> SELECTION_OPTION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillData.CODEC.fieldOf("skill").forGetter(o -> o.skillData),
            Codec.STRING.fieldOf("equipmentSlot").forGetter(o -> o.equipmentSlot),
            Codec.INT.fieldOf("localIndex").forGetter(o -> o.localIndex),
            Codec.INT.fieldOf("globalIndex").forGetter(o -> o.globalIndex),
            Codec.STRING.fieldOf("priority").forGetter(o -> o.priority.name())
    ).apply(builder, (skill, equipmentSlot, local, global, priority) ->
            new SelectionOption(skill, equipmentSlot, local, global, SkillSelectionPriority.valueOf(priority))));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSelectionManager> STREAM_CODEC = StreamCodec.composite(
            // todo: dedicated stream codec would be more efficient
            ByteBufCodecs.fromCodec(Codec.list(SELECTION_OPTION_CODEC)),
            SkillSelectionManager::getAllOptions,
            SkillSelection.STREAM_CODEC,
            SkillSelectionManager::getSkillSelection,
            ByteBufCodecs.VAR_INT,
            SkillSelectionManager::getSelectionIndex,
            ByteBufCodecs.BOOL,
            SkillSelectionManager::isSelectionValid,
            SkillSelectionManager::fromNetwork
    );

    private final SkillSelection skillSelection;
    private final List<SelectionOption> options = new ArrayList<>();

    private int selectionIndex = -1;
    private boolean selectionValid = false;

    public SkillSelectionManager(SkillSelection skillSelection) {
        this.skillSelection = skillSelection;
    }

    public SkillSelection getSkillSelection() {
        return skillSelection;
    }

    public void refresh(Player player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }
        options.clear();
        selectionValid = false;

        var event = new GatherSkillSelectionEvent(player);
        gatherDefaultSources(player, event);
        NeoForge.EVENT_BUS.post(event);

        var sortedSources = new ArrayList<>(event.getSources());
        sortedSources.sort(Comparator.comparingInt(source -> source.priority().sortOrder()));

        for (var source : sortedSources) {
            addFromContainer(source.container(), source.equipmentSlot(), source.priority());
        }

        if (!selectionValid && !options.isEmpty()) {
            tryLastSelectionOrDefault();
        }
        if (selectionIndex == -1 && !options.isEmpty()) {
            selectionIndex = 0;
        }
    }

    public void replaceFrom(SkillSelectionManager other) {
        options.clear();
        for (SelectionOption option : other.options) {
            options.add(option.copy());
        }
        skillSelection.copyFrom(other.skillSelection);
        selectionIndex = other.selectionIndex;
        selectionValid = other.selectionValid;
    }

    private static SkillSelectionManager fromNetwork(
            List<SelectionOption> options,
            SkillSelection skillSelection,
            int selectionIndex,
            boolean selectionValid) {
        var manager = new SkillSelectionManager(skillSelection);
        manager.options.addAll(options);
        manager.selectionIndex = selectionIndex;
        manager.selectionValid = selectionValid;
        return manager;
    }

    private void gatherDefaultSources(Player player, GatherSkillSelectionEvent event) {
        addContainerFromSlot(player, event, EquipmentSlot.HEAD, SkillSelectionPriority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.CHEST, SkillSelectionPriority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.LEGS, SkillSelectionPriority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.FEET, SkillSelectionPriority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.MAINHAND, SkillSelectionPriority.HANDHELD);
        addContainerFromSlot(player, event, EquipmentSlot.OFFHAND, SkillSelectionPriority.HANDHELD);
    }

    private static void addContainerFromSlot(Player player, GatherSkillSelectionEvent event, EquipmentSlot slot, SkillSelectionPriority priority) {
        ItemStack stack = player.getItemBySlot(slot);
        if (ISkillContainer.isSkillContainer(stack)) {
            event.addSource(ISkillContainer.get(stack), slot, priority);
        }
    }

    private void addFromContainer(ISkillContainer container, String equipmentSlot, SkillSelectionPriority priority) {
        if (!(container.isSpellWheel() && (!container.mustEquip() || !isHandSlot(equipmentSlot)))) {
            return;
        }
        for (SkillSlot skillSlot : container.getActiveSpells()) {
            int globalIndex = addOrMergeSelectionOption(new SelectionOption(skillSlot.skillData(), equipmentSlot, skillSlot.index(), options.size(), priority));
            if (globalIndex >= 0
                    && !skillSelection.isEmpty()
                    && skillSelection.index() == skillSlot.index()
                    && Objects.equals(skillSelection.equipmentSlot(), equipmentSlot)) {
                selectionIndex = globalIndex;
                selectionValid = true;
            }
        }
    }

    private static boolean isHandSlot(String equipmentSlot) {
        return MAINHAND.equals(equipmentSlot) || OFFHAND.equals(equipmentSlot);
    }

    private int addOrMergeSelectionOption(SelectionOption option) {
        SelectionOption existing = findExistingSkill(option.skillData.skillId());
        if (existing != null) {
            if (option.skillData.getLevel() > existing.skillData.getLevel()) {
                option.globalIndex = existing.globalIndex;
                options.set(existing.globalIndex, option);
                return existing.globalIndex;
            }
            return -1;
        }
        option.globalIndex = options.size();
        options.add(option);
        return option.globalIndex;
    }

    private @Nullable SelectionOption findExistingSkill(ResourceLocation skillId) {
        for (SelectionOption option : options) {
            if (option.skillData.skillId().equals(skillId)) {
                return option;
            }
        }
        return null;
    }

    private void tryLastSelectionOrDefault() {
        if (skillSelection.lastIndex() < 0 || skillSelection.lastEquipmentSlot() == null) {
            options.stream().findFirst().ifPresent(selection ->
                    makeLocalSelection(selection.equipmentSlot, selection.localIndex, selection.globalIndex, false));
        } else {
            var spellsForSource = getOptionsForSlot(skillSelection.lastEquipmentSlot());
            if (!spellsForSource.isEmpty()) {
                if (skillSelection.lastIndex() < spellsForSource.size()) {
                    var selection = spellsForSource.get(skillSelection.lastIndex());
                    makeLocalSelection(skillSelection.lastEquipmentSlot(), skillSelection.lastIndex(), selection.globalIndex, false);
                } else {
                    var selection = spellsForSource.getFirst();
                    makeLocalSelection(skillSelection.lastEquipmentSlot(), 0, selection.globalIndex, false);
                }
            }
        }
    }

    private void makeLocalSelection(String equipmentSlot, int slotIndex, int globalIndex, boolean syncToServer) {
        selectionIndex = globalIndex;
        selectionValid = true;
        if (syncToServer) {
            skillSelection.makeSelection(equipmentSlot, slotIndex);
            SelectSkillPacket.send(skillSelection.copy());
        }
    }

    /**
     * Client-side selection change. Applies locally and sends the selection to the server without waiting for ack.
     */
    public void makeSelection(int globalIndex) {
        if (globalIndex != selectionIndex && globalIndex >= 0 && globalIndex < options.size()) {
            var option = options.get(globalIndex);
            makeLocalSelection(option.equipmentSlot, option.localIndex, globalIndex, true);
        }
    }

    /**
     * Server-side application of a client-sent selection. Validates against the current option list.
     */
    public boolean applySelection(SkillSelection incoming) {
        if (incoming.isEmpty() || !isValidSelection(incoming)) {
            return false;
        }
        skillSelection.copyFrom(incoming);
        reconcileSelectionIndexFromPointer();
        return selectionValid;
    }

    private boolean isValidSelection(SkillSelection incoming) {
        if (incoming.isEmpty() || incoming.equipmentSlot() == null) {
            return false;
        }
        for (SelectionOption option : options) {
            if (option.equipmentSlot.equals(incoming.equipmentSlot()) && option.localIndex == incoming.index()) {
                return true;
            }
        }
        return false;
    }

    private void reconcileSelectionIndexFromPointer() {
        selectionValid = false;
        for (SelectionOption option : options) {
            if (Objects.equals(option.equipmentSlot, skillSelection.equipmentSlot()) && option.localIndex == skillSelection.index()) {
                selectionIndex = option.globalIndex;
                selectionValid = true;
                return;
            }
        }
        selectionIndex = -1;
    }

    public boolean isSelectionValid() {
        return selectionValid;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public int getGlobalSelectionIndex() {
        var selection = getSelection();
        return selection == null ? -1 : selection.globalIndex;
    }

    public @Nullable SelectionOption getSelection() {
        if (selectionIndex >= 0 && selectionIndex < options.size()) {
            return options.get(selectionIndex);
        } else if (!options.isEmpty()) {
            return options.getFirst();
        }
        return null;
    }

    public @Nullable SkillData getSelectedSkillData() {
        if (selectionIndex >= 0 && selectionIndex < options.size()) {
            return options.get(selectionIndex).skillData;
        }
        return null;
    }

    public @Nullable SelectionOption getOptionAt(int index) {
        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return null;
    }

    public @Nullable SkillData getSkillData(int index) {
        var option = getOptionAt(index);
        return option == null ? null : option.skillData;
    }

    public @NotNull List<SelectionOption> getAllOptions() {
        return options;
    }

    public @NotNull List<SelectionOption> getOptionsForSlot(String equipmentSlot) {
        return options.stream().filter(option -> option.equipmentSlot.equals(equipmentSlot)).toList();
    }

    public @Nullable SkillData getSkillForSlot(String equipmentSlot, int index) {
        var spells = getOptionsForSlot(equipmentSlot);
        if (index >= 0 && index < spells.size()) {
            return spells.get(index).skillData;
        }
        return null;
    }

    public int getSkillCount() {
        return options.size();
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }

    public static final class SelectionOption {
        public SkillData skillData;
        public String equipmentSlot;
        public int localIndex;
        public int globalIndex;
        public SkillSelectionPriority priority;

        public SelectionOption(SkillData skillData, String equipmentSlot, int localIndex, int globalIndex, SkillSelectionPriority priority) {
            this.skillData = skillData;
            this.equipmentSlot = equipmentSlot;
            this.localIndex = localIndex;
            this.globalIndex = globalIndex;
            this.priority = priority;
        }

        public AbstractSkill getSkill() {
            return skillData.getSkill();
        }

        public int getLevel() {
            return skillData.getLevel();
        }

        SelectionOption copy() {
            return new SelectionOption(skillData, equipmentSlot, localIndex, globalIndex, priority);
        }
    }
}
