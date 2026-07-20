package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public interface IDrinkPotions {

    boolean isDrinkingPotion();

    void setDrinkingPotion(boolean drinkingPotion);

    void startDrinkingPotion();

    void finishDrinkingPotion();

    int getDrinkingTime();

    void setDrinkingTime(int drinkingTime);

    @NotNull SoundEvent getPotionDrinkingSound();

    default <T extends Mob & IDrinkPotions> void handlePotionTick(T mob) {
        if (isDrinkingPotion()) {
            int drinkingTime = getDrinkingTime() - 1;
            if (drinkingTime <= 0) {
                finishDrinkingPotion();
                setDrinkingPotion(false);
            } else if (drinkingTime % 4 == 0) {
                if (!mob.isSilent()) {
                    mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), getPotionDrinkingSound(), mob.getSoundSource(), 1.0F, Utils.random.nextFloat() * 0.1F + 0.9F);
                }
            }
            setDrinkingTime(drinkingTime);
        }
    }
}
