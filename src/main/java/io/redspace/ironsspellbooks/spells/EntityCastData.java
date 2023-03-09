package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;

public class EntityCastData implements CastData {
    private final Entity castingEntity;

    public EntityCastData(@Nonnull Entity entity) {
        this.castingEntity = entity;
    }

    public Entity getCastingEntity() {
        return this.castingEntity;
    }

    public void discardCastingEntity() {
        if (this.castingEntity != null) {
            this.castingEntity.discard();
        }
    }

    @Override
    public void reset() {
        discardCastingEntity();
    }
}
