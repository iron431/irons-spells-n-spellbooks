package io.redspace.skillcasting.data.cast;

import net.minecraft.world.entity.Entity;

public interface PreventDismount {

    default boolean canEntityDismount(Entity entity) {
        return false;
    }
}
