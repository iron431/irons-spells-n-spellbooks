package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public interface IAbilityHandler<T extends Mob & IAbilityHandler<T>> extends IAnimatedAttacker {
    @SuppressWarnings("unchecked")
    default void activateAbility(MobAbilityType<T> ability, boolean interrupt) {
        var activeAbility = getActiveAbility();
        if (activeAbility == null) {
            activeAbility = ability.createInstance((T) this);
            activeAbility.onStart();
        } else if (interrupt) {
            activeAbility.onFinish();
            setActiveAbility(ability.createInstance((T) this));
            activeAbility.onStart();
        }
    }

    default void activateAbility(MobAbilityType<T> type) {
        activateAbility(type, false);
    }

    default void stopActiveAbility() {
        var activeAbility = getActiveAbility();
        if (activeAbility == null) {
            return;
        }
        activeAbility.onFinish();
        setActiveAbility(null);
    }

    @SuppressWarnings("unchecked")
    default void handleAbilityTicking() {
        T mob = (T) this;
        if (mob.level.isClientSide) {
            return;

        }
        var activeAbility = getActiveAbility();
        if (activeAbility != null) {
            activeAbility.tick();
            if (activeAbility.isFinished()) {
                stopActiveAbility();
            }
        }
    }

    default boolean isUsingAbility() {
        return getActiveAbility() != null;
    }

    @Nullable MobAbilityInstance<T> getActiveAbility();

    void setActiveAbility(@Nullable MobAbilityInstance<T> abilityInstance);
}
