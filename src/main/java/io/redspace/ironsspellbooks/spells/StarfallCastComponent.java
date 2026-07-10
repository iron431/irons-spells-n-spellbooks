package io.redspace.ironsspellbooks.spells;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StarfallCastComponent {
    public Vec3 center;
    public final List<UUID> trackedEntityIds = new ArrayList<>();

    public StarfallCastComponent(Vec3 center) {
        this.center = center;
    }

    public void updateTrackedEntities(List<Entity> entities) {
        trackedEntityIds.clear();
        entities.forEach(entity -> trackedEntityIds.add(entity.getUUID()));
    }
}
