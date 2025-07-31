package io.redspace.ironsspellbooks.entity.spells.root;

import net.minecraft.world.entity.Entity;

public interface PreventDismount {

    default boolean canEntityDismount(Entity entity){
        return false;
    }
}
