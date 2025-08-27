package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffectCategory;

public class ChargeEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final float ATTACK_DAMAGE_PER_LEVEL = .1f;
    public static final float SPEED_PER_LEVEL = .2f;
    public static final float SPELL_POWER_PER_LEVEL = .05f;

    public ChargeEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

}
