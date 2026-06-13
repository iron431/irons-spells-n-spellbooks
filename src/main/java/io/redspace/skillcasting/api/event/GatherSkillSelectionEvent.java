package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.ISkillContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GatherSkillSelectionEvent extends PlayerEvent {

    public record Source(@NotNull ISkillContainer container, @NotNull String equipmentSlot, @NotNull SkillSelectionPriority priority) {
    }

    private final List<Source> sources = new ArrayList<>();

    public GatherSkillSelectionEvent(Player player) {
        super(player);
    }

    public void addSource(ISkillContainer container, String equipmentSlot, SkillSelectionPriority priority) {
        if (container == null || equipmentSlot == null || priority == null) {
            return;
        }
        sources.add(new Source(container, equipmentSlot, priority));
    }

    public void addSource(ISkillContainer container, EquipmentSlot slot, SkillSelectionPriority priority) {
        addSource(container, slot.getName(), priority);
    }

    public List<Source> getSources() {
        return sources;
    }
}
