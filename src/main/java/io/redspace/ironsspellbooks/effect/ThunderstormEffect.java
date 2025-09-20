package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.LightningStrike;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ThunderstormEffect extends MagicMobEffect {
    public ThunderstormEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int pAmplifier) {
        var radiusSqr = 400; //20
        entity.level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(20, 12, 20),
                        livingEntity -> livingEntity != entity &&
                                horizontalDistanceSqr(livingEntity, entity) < radiusSqr &&
                                livingEntity.isPickable() &&
                                !livingEntity.isSpectator() &&
                                !Utils.shouldHealEntity(entity, livingEntity) &&
                                Utils.hasLineOfSight(entity.level, entity, livingEntity, false)
                )
                .forEach(targetEntity -> {
                    LightningStrike lightningStrike = new LightningStrike(entity.level);
                    lightningStrike.setOwner(entity);
                    lightningStrike.setDamage(getDamageFromAmplifier(pAmplifier, entity));
                    lightningStrike.setPos(targetEntity.position());
                    entity.level.addFreshEntity(lightningStrike);
                });
    }

    private float horizontalDistanceSqr(LivingEntity livingEntity, LivingEntity entity2) {
        var dx = livingEntity.getX() - entity2.getX();
        var dz = livingEntity.getZ() - entity2.getZ();
        return (float) (dx * dx + dz * dz);
    }

    public static float getDamageFromAmplifier(int effectAmplifier, @Nullable LivingEntity caster) {
        var power = caster == null ? 1 : SpellRegistry.THUNDERSTORM_SPELL.get().getEntityPowerMultiplier(caster);
        return (((effectAmplifier - 7) * power) + 7);
    }
}
