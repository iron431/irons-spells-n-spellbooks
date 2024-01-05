package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SpiderAspectEffect extends MagicMobEffect {
    public static final float DAMAGE_PER_LEVEL = .05f;
    public SpiderAspectEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}
