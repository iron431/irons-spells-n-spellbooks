package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.List;

public class BurningDashEffect extends MobEffect {
    public BurningDashEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        List<Entity> list = livingEntity.level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(.25, .5, .25));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (entity instanceof LivingEntity) {
                    DamageSources.applyDamage(entity, amplifier, SpellRegistry.BURNING_DASH_SPELL.get().getDamageSource(livingEntity));
                    //Guarantee that the entity receives i-frames, since we are damaging every tick
                    entity.invulnerableTime = 20;
                }
            }
        } else if (livingEntity.horizontalCollision) {
            livingEntity.removeEffect(this);
        }
        livingEntity.fallDistance = 0;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        pLivingEntity.setLivingEntityFlag(4, true);

    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        pLivingEntity.setLivingEntityFlag(4, false);
    }
}
