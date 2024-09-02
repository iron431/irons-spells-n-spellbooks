package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class EntityCastContext implements ICastContext {
    @Nullable
    @Override
    public Entity getEntity() {
        return null;
    }

    @Override
    public Vec3 getPosition() {
        return null;
    }

    @Override
    public float getXRot() {
        return 0;
    }

    @Override
    public float getYRot() {
        return 0;
    }

    @Override
    public int getSpellLevel() {
        return 0;
    }

    @Override
    public void setSpellLevel(int spellLevel) {

    }

    @Override
    public @NotNull CastSource getCastSource() {
        return null;
    }

    @Nullable
    @Override
    public String getCastingEquipmentSlot() {
        return "";
    }

    @Nullable
    @Override
    public ICastData getCastData() {
        return null;
    }

    @Override
    public @NotNull MagicData getMagicData() {
        return null;
    }
}
