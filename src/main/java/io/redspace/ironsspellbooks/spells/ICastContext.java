package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.swing.text.html.parser.Entity;

public interface ICastContext {
    @Nullable Entity getEntity();
    Vec3 getPosition();
    float getXRot();
    float getYRot();
    int getSpellLevel();
    @Nullable CastSource getCastSource();
    @Nullable String getCastingEquipmentSlot();

    //TODO: Not sure if these should be here.  Need to get further along and decide
    @Nullable ICastData getCastData();
    @Nullable MagicData getMagicData();

}
