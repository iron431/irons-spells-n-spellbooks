package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsAttributeCompatMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RendEffect extends MobEffect implements IBackwardsAttributeCompatMobEffect {
    public static final float ARMOR_PER_LEVEL = -.05f;

    public RendEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}
