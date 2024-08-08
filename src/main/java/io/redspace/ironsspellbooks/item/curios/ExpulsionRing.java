package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class ExpulsionRing extends PassiveAbilityCurio {
    public static final int COOLDOWN_IN_TICKS = 10 * 20;
    public static final int RADIUS = 4;
    public static final int RADIUS_SQR = RADIUS * RADIUS;

    public ExpulsionRing() {
        super(new Properties().stacksTo(1), Curios.RING_SLOT);
    }

    @Override
    protected int getCooldownTicks() {
        return COOLDOWN_IN_TICKS;
    }

    @SubscribeEvent
    public static void handleAbility(LivingIncomingDamageEvent event) {
        var RING = ((ExpulsionRing) ItemRegistry.EXPULSION_RING.get());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (event.getSource().getEntity() != null && RING.isEquippedBy(serverPlayer) && RING.tryProcCooldown(serverPlayer)) {
                var vec = serverPlayer.getBoundingBox().getCenter();
                //Visual Explosion
                serverPlayer.level.explode(
                        null,
                        null,
                        null,
                        vec.x,
                        vec.y,
                        vec.z,
                        0,
                        false,
                        Level.ExplosionInteraction.NONE,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.WIND_CHARGE_BURST
                );
                //Knockback effect
                serverPlayer.level.getEntities(serverPlayer, serverPlayer.getBoundingBox().inflate(3)).forEach(entity -> {
                    var d = Math.max(entity.distanceToSqr(serverPlayer), .2);
                    if (d < RADIUS_SQR && !DamageSources.isFriendlyFireBetween(serverPlayer, entity)) {
                        var f = 1 - d / (RADIUS_SQR) + .6f * (entity instanceof LivingEntity living ? 1 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 1);
                        var impulse = entity.getBoundingBox().getCenter().subtract(serverPlayer.getBoundingBox().getCenter()).normalize().add(0, 0.1, 0).scale(f);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(impulse));
                        entity.hurtMarked = true;
                    }
                });
            }
        }
    }
}
