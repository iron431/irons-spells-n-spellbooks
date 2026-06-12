package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.ISkillContainer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GatherSkillSelectionEvent extends PlayerEvent {

    //todo: this deserves to get elevated to its own class due to use in skill bar overlay
    public enum Priority {
        PRIMARY_SKILL_SOURCE(0),
        CURIO(100),
        ARMOR(200),
        HANDHELD(300),
        OTHER(1000);

        private final int sortOrder;

        Priority(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public int sortOrder() {
            return sortOrder;
        }
    }

    public record Source(@NotNull ISkillContainer container, @NotNull String sourceId, @NotNull Priority priority) {
    }

    private final List<Source> sources = new ArrayList<>();

    public GatherSkillSelectionEvent(Player player) {
        super(player);
    }

    public void addSource(ISkillContainer container, String sourceId, Priority priority) {
        if (container == null || sourceId == null || priority == null) {
            return;
        }
        sources.add(new Source(container, sourceId, priority));
    }

    public List<Source> getSources() {
        return sources;
    }
}
