package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.spells.DefaultConfig;
import io.redspace.ironsspellbooks.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface ISpell {
    ResourceLocation getSpellId();
    SchoolType getSchool();
    CastType getCastType();
    int getCastTime();
    int getBaseManaCost();
    int getBaseSpellPower();
    DefaultConfig getDefaultConfig();

    void onCast(Level level, LivingEntity entity, @Nullable MagicData playerMagicData);
    void onClientCast(Level level, LivingEntity entity, @Nullable ICastData castData);
    void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable MagicData playerMagicData);
    void onServerPreCast(Level level, LivingEntity entity, @Nullable MagicData playerMagicData);
    void onServerCastTick(Level level, LivingEntity entity, @Nullable MagicData playerMagicData);
    void onServerCastComplete(Level level, LivingEntity entity, @Nullable MagicData playerMagicData, boolean cancelled);
}
