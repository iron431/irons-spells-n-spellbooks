package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.echoing_strikes.EchoingSword;
import io.redspace.ironsspellbooks.entity.spells.echoing_strikes.EchoingArrowProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.ender.EchoingStrikesSpell;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import javax.annotation.Nullable;

@EventBusSubscriber
public class EchoingStrikesEffect extends MagicMobEffect {
    public static final float PERCENT_PER_AMPLIFIER = .05f;
    public EchoingStrikesEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectStarted(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectStarted(pLivingEntity, pAmplifier);
        // default count
        EchoingStrikesData.get(pLivingEntity).setHitCount(1);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
        EchoingStrikesData.remove(pLivingEntity);
    }

    @SubscribeEvent
    public static void createEcho(LivingDamageEvent.Post event) {
        var damageSource = event.getSource();
        if (damageSource.getEntity() instanceof LivingEntity attacker && (damageSource.getDirectEntity() == attacker || damageSource.getDirectEntity() instanceof AbstractArrow) && !(damageSource instanceof SpellDamageSource)) {
            var effect = attacker.getEffect(MobEffectRegistry.ECHOING_STRIKES);
            if (effect == null) {
                return;
            }
            var data = EchoingStrikesData.get(attacker);
            if (!data.hasHitsRemaining()) {
                attacker.removeEffect(MobEffectRegistry.ECHOING_STRIKES);
                return;
            }
            var level = attacker.level();
            var percent = getDamageModifier(effect.getAmplifier(), attacker);
            var target = event.getEntity();
            if (damageSource.isDirect()) {
                createEchoingSword(attacker, level, target, event.getNewDamage() * percent);
            } else {
                createEchoingArrow(attacker, level, target, event.getNewDamage() * percent);
            }

            data.decrementHit();
            if (!data.hasHitsRemaining()) {
                attacker.removeEffect(MobEffectRegistry.ECHOING_STRIKES);
            }
        }
    }

    private static void createEchoingArrow(LivingEntity attacker, Level level, LivingEntity target, float damage) {
        EchoingArrowProjectile arrow = new EchoingArrowProjectile(EntityRegistry.ECHOING_ARROW.get(), level);
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 arrowPos = attacker.position().lerp(targetPos, 0.35);
        Vec3 right = targetPos.subtract(arrowPos).normalize().cross(new Vec3(0, 1, 0));
        Vec3 spawnPos = arrowPos
                .add(right.scale((1 + level.getRandom().nextFloat() * 4) * (level.getRandom().nextBoolean() ? 1 : -1)))
                .add(0, (level.getRandom().nextFloat() - 0.25) * 6, 0);
        arrow.moveTo(spawnPos);
        arrow.setHomingTarget(target);
        arrow.setDamage(damage);
        arrow.setOwner(attacker);
        attacker.level.addFreshEntity(arrow);
    }

    private static void createEchoingSword(LivingEntity attacker, Level level, LivingEntity target, float damage) {
        EchoingSword echo = new EchoingSword(EntityRegistry.ECHOING_SWORD.get(), level);
        // todo: real spawn logic
        echo.moveTo(target.getBoundingBox().getCenter().add(new Vec3(2.5, 0, 0).yRot(level.getRandom().nextFloat() * Mth.TWO_PI)).add(Utils.getRandomVec3(1.75)));
        echo.setHomingTarget(target);
        echo.moveAndRotateTowards(target.getBoundingBox().getCenter());
        echo.setExplosionRadius(EchoingStrikesSpell.radius);
        echo.setDamage(damage);
        echo.setOwner(attacker);
        attacker.level.addFreshEntity(echo);
    }

    public static float getDamageModifier(int effectAmplifier, @Nullable LivingEntity caster) {
        var power = caster == null ? 1 : SpellRegistry.ECHOING_STRIKES_SPELL.get().getEntityPowerMultiplier(caster);
        return (effectAmplifier + 1) * power * PERCENT_PER_AMPLIFIER;
    }
}
