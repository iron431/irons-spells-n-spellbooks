package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;


public interface ICastContext {
    @Nullable
    Entity getEntity();
    Vec3 getPosition();
    float getXRot();
    float getYRot();
    int getSpellLevel();
    void setSpellLevel(int spellLevel);
    @NotNull CastSource getCastSource();
    @Nullable String getCastingEquipmentSlot();

    //TODO: Not sure if these should be here.  Need to get further along and decide
    @Nullable ICastData getCastData();
    @NotNull MagicData getMagicData();
}
