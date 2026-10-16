package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GatherSkillSelectionEvent extends PlayerEvent {

    public record Source(@NotNull ISkillContainer container, @NotNull CastSource castSource,
                         @NotNull SkillSelectionPriority priority) {
    }

    private final List<Source> sources = new ArrayList<>();

    public GatherSkillSelectionEvent(Player player) {
        super(player);
    }


    public void addSource(@NotNull ISkillContainer container, @NotNull CastSource equipmentSlot, @NotNull SkillSelectionPriority priority) {
        sources.add(new Source(container, equipmentSlot, priority));
    }

    /**
     * Adds a container found in an equipment slot, generating the cast source based on the slot and data component it comes from
     */
    public void addSource(@NotNull ISkillContainer container, @NotNull DataComponentType<?> origin, @NotNull EquipmentSlot slot, @NotNull SkillSelectionPriority priority) {
        ResourceLocation originId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(origin);
        String type = originId == null ? slot.getName() : originId.toString();
        addSource(container, CastSource.of(type, slot), priority);
    }

    /**
     * Adds a container found in an equipment slot using the container's own {@link ISkillContainer#getSelectionPriority()}.
     */
    public void addSource(@NotNull ISkillContainer container, @NotNull DataComponentType<?> origin, @NotNull EquipmentSlot slot) {
        addSource(container, origin, slot, container.getSelectionPriority());
    }

    public void clearSourcesBySlot(String equipmentSlot) {
        sources.removeIf(source -> source.castSource.equipmentSlot().equals(equipmentSlot));
    }

    public List<Source> getSources() {
        return sources;
    }
}
