package io.redspace.skillcasting.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.lifecycle.CastSource;
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

public final class SkillSelectionManager {

    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();

    private static final Codec<SelectionOption> SELECTION_OPTION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillData.CODEC.fieldOf("skill").forGetter(o -> o.skillData),
            CastSource.CODEC.fieldOf("source").forGetter(o -> o.castSource),
            Codec.INT.fieldOf("localIndex").forGetter(o -> o.localIndex),
            Codec.INT.fieldOf("globalIndex").forGetter(o -> o.globalIndex),
            Codec.STRING.fieldOf("priority").forGetter(o -> o.priority.name())
    ).apply(builder, (skill, source, local, global, priority) ->
            new SelectionOption(skill, source, local, global, GatherSkillSelectionEvent.Priority.valueOf(priority))));

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
            addFromContainer(source.container(), source.sourceId(), source.priority());
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
        addContainerFromSlot(player, event, EquipmentSlot.HEAD, GatherSkillSelectionEvent.Priority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.CHEST, GatherSkillSelectionEvent.Priority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.LEGS, GatherSkillSelectionEvent.Priority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.FEET, GatherSkillSelectionEvent.Priority.ARMOR);
        addContainerFromSlot(player, event, EquipmentSlot.MAINHAND, GatherSkillSelectionEvent.Priority.HANDHELD);
        addContainerFromSlot(player, event, EquipmentSlot.OFFHAND, GatherSkillSelectionEvent.Priority.HANDHELD);
    }

    private static void addContainerFromSlot(Player player, GatherSkillSelectionEvent event, EquipmentSlot slot, GatherSkillSelectionEvent.Priority priority) {
        ItemStack stack = player.getItemBySlot(slot);
        if (ISkillContainer.isSkillContainer(stack)) {
            event.addSource(ISkillContainer.get(stack), slot.getName(), priority);
        }
    }

    private void addFromContainer(ISkillContainer container, CastSource castSource, GatherSkillSelectionEvent.Priority priority) {
        if (!(container.isSpellWheel() && (!container.mustEquip() || !isHandSlot(castSource)))) {
            return;
        }
        for (SkillSlot skillSlot : container.getActiveSpells()) {
            int globalIndex = addOrMergeSelectionOption(new SelectionOption(skillSlot.skillData(), castSource, skillSlot.index(), options.size(), priority));
            if (globalIndex >= 0
                    && skillSelection.index() == skillSlot.index()
                    && skillSelection.sourceId().equals(sourceId)) {
                selectionIndex = globalIndex;
                selectionValid = true;
            }
        }
    }

    private static boolean isHandSlot(CastSource source) {
        return source.equipmentSource().isPresent() && (source.equipmentSource().get().equals(MAINHAND) || source.equipmentSource().get().equals(OFFHAND));
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
        if (skillSelection.lastSourceId().isEmpty()) {
            options.stream().findFirst().ifPresent(selection ->
                    makeLocalSelection(selection.castSource, selection.localIndex, selection.globalIndex, false));
        } else if (skillSelection.lastIndex() != -1) {
            var spellsForSource = getOptionsForSource(skillSelection.lastSourceId());
            if (!spellsForSource.isEmpty()) {
                if (skillSelection.lastIndex() < spellsForSource.size()) {
                    var selection = spellsForSource.get(skillSelection.lastIndex());
                    makeLocalSelection(skillSelection.lastSourceId(), skillSelection.lastIndex(), selection.globalIndex, false);
                } else {
                    var selection = spellsForSource.getFirst();
                    makeLocalSelection(skillSelection.lastSourceId(), 0, selection.globalIndex, false);
                }
            }
        }
    }

    private void makeLocalSelection(CastSource source, int slotIndex, int globalIndex, boolean syncToServer) {
        selectionIndex = globalIndex;
        selectionValid = true;
        if (syncToServer) {
            skillSelection.makeSelection(source, slotIndex);
            SelectSkillPacket.send(skillSelection.copy());
        }
    }

    /**
     * Client-side selection change. Applies locally and sends the selection to the server without waiting for ack.
     */
    public void makeSelection(int globalIndex) {
        if (globalIndex != selectionIndex && globalIndex >= 0 && globalIndex < options.size()) {
            var option = options.get(globalIndex);
            makeLocalSelection(option.castSource, option.localIndex, globalIndex, true);
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
        if (incoming.sourceId().isEmpty() || incoming.index() < 0) {
            return false;
        }
        for (SelectionOption option : options) {
            if (option.castSource.equals(incoming.sourceId()) && option.localIndex == incoming.index()) {
                return true;
            }
        }
        return false;
    }

    private void reconcileSelectionIndexFromPointer() {
        selectionValid = false;
        for (SelectionOption option : options) {
            if (option.castSource.equals(skillSelection.sourceId()) && option.localIndex == skillSelection.index()) {
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

    public @NotNull List<SelectionOption> getOptionsForSource(String sourceId) {
        return options.stream().filter(option -> option.castSource.equals(sourceId)).toList();
    }

    public @Nullable SkillData getSkillForSource(String sourceId, int index) {
        var spells = getOptionsForSource(sourceId);
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
        public CastSource castSource;
        public int localIndex;
        public int globalIndex;
        public GatherSkillSelectionEvent.Priority priority;

        public SelectionOption(SkillData skillData, CastSource castSource, int localIndex, int globalIndex, GatherSkillSelectionEvent.Priority priority) {
            this.skillData = skillData;
            this.castSource = castSource;
            this.localIndex = localIndex;
            this.globalIndex = globalIndex;
            this.priority = priority;
        }

        SelectionOption copy() {
            return new SelectionOption(skillData, castSource, localIndex, globalIndex, priority);
        }
    }
}
