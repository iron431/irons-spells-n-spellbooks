package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;

@EventBusSubscriber
public class FrostbiteEffect extends MagicMobEffect {
    public FrostbiteEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void handleFrostbiteDeathEffects(LivingDeathEvent event) {
        var damageSource = event.getSource();
        LivingEntity entity = event.getEntity();
        if (damageSource != null && damageSource.getEntity() instanceof LivingEntity attacker
        ) {
            var effect = attacker.getEffect(MobEffectRegistry.FROSTBITTEN_STRIKES);
            if (effect != null && entity.isFullyFrozen()) {
                FrozenHumanoid iceClone = new FrozenHumanoid(entity.level, entity);
                iceClone.setSummoner(attacker);
                iceClone.setShatterDamage(getDamageForAmplifier(effect.getAmplifier(), attacker));
                iceClone.setDeathTimer(20 * 5);
                entity.level.addFreshEntity(iceClone);
                entity.deathTime = 1000;
                iceClone.playSound(SoundRegistry.FROSTBITE_FREEZE.get(), 2, Utils.random.nextInt(9, 11) * .1f);
            }
        }
    }

    public static float getDamageForAmplifier(int effectAmplifier, @Nullable LivingEntity caster) {
        var power = caster == null ? 1 : SpellRegistry.FROSTBITE_SPELL.get().getEntityPowerMultiplier(caster);
        return (1 + effectAmplifier) * power;
    }
}
