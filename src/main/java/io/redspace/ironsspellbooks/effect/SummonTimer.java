package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class SummonTimer extends MobEffect {
    public SummonTimer(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        IronsSpellbooks.LOGGER.debug("Summoner Timer Wore out on {}", pLivingEntity.getName().getString());
        if(pLivingEntity instanceof MagicSummon summon)
            summon.onUnSummon();

    }
}
