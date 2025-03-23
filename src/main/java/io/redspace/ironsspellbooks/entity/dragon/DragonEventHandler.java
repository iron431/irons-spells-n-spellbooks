package io.redspace.ironsspellbooks.entity.dragon;

import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber
public class DragonEventHandler {

    @SubscribeEvent
    public static void fixDragonCrits(CriticalHitEvent event) {
        // Crits require the target to be a LivingEntity, meaning dragon parts cannot be critically struck
        // Re-evaluate default crit criteria (without the living entity check, ofc)
        if (event.getTarget() instanceof DragonPartEntity dragonPartEntity) {
            var attacker = event.getEntity();
            var defaultShouldCrit = attacker.getAttackStrengthScale(0.5f) > .9
                    && attacker.fallDistance > 0.0F
                    && !attacker.onGround()
                    && !attacker.onClimbable()
                    && !attacker.isInWater()
                    && !attacker.hasEffect(MobEffects.BLINDNESS)
                    && !attacker.isPassenger()
                    && !attacker.isSprinting();
            event.setCriticalHit(defaultShouldCrit);
            if (event.getDamageMultiplier() == 1) {
                event.setDamageMultiplier(1.5f);
            }
        }
    }
}
