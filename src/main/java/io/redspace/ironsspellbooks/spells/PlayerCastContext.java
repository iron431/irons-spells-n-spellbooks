package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCastContext extends BaseCastContext implements ICastContext {
    Player player;

    public PlayerCastContext(Player player, int spellLevel, CastSource castSource) {
        super(spellLevel, castSource);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return player;
    }

    @Override
    public Vec3 getPosition() {
        return player.position();
    }

    @Override
    public float getXRot() {
        return player.getXRot();
    }

    @Override
    public float getYRot() {
        return player.getYRot();
    }

    @Nullable
    @Override
    public String getCastingEquipmentSlot() {
        return MagicData.getMagicData(player).getCastingEquipmentSlot();
    }

    @Nullable
    @Override
    public ICastData getCastData() {
        return null;
    }

    @Override
    public @NotNull MagicData getMagicData() {
        return MagicData.getMagicData(player);
    }
}
