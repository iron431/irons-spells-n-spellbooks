package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RendEffect extends MobEffect {
    public static final float ARMOR_PER_LEVEL = -.05f;
    public RendEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}
