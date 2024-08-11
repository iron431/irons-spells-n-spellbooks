package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.parser.Entity;

public class PlayerCastContext implements ICastContext {
    PlayerCastContext(Player player) {

    }

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

    @Nullable
    @Override
    public CastSource getCastSource() {
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

    @Nullable
    @Override
    public MagicData getMagicData() {
        return null;
    }
}
