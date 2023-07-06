package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.DefaultConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.*;

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

    default Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    default Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    default ICastDataSerializable getEmptyCastData() {
        return null;
    }

    default AnimationHolder getCastStartAnimation() {
        return switch (getCastType()) {
            case INSTANT -> ANIMATION_INSTANT_CAST;
            case CONTINUOUS -> ANIMATION_CONTINUOUS_CAST;
            case LONG -> ANIMATION_LONG_CAST;
            default -> AnimationHolder.none();
        };
    }

    default AnimationHolder getCastFinishAnimation() {
        return switch (getCastType()) {
            case LONG -> ANIMATION_LONG_CAST_FINISH;
            default -> AnimationHolder.none();
        };
    }
}
