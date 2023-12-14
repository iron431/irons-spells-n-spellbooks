package io.redspace.ironsspellbooks.effect.guiding_bolt;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.A;
import oshi.software.os.mac.MacOSThread;

import java.util.*;

@Mod.EventBusSubscriber
public class GuidingBoltManager {

    private static final HashMap<UUID, ArrayList<Projectile>> TRACKED_ENTITIES = new HashMap<>();


    @SubscribeEvent
    public static void onProjectileShot(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getEntity() instanceof Projectile projectile) {
                IronsSpellbooks.LOGGER.debug("on projectile shoot: {}", event.getEntity());
                for (Map.Entry<UUID, ArrayList<Projectile>> entry : TRACKED_ENTITIES.entrySet()) {
                    var entity = serverLevel.getEntity(entry.getKey());
                    if (entity != null) {
                        int searchRange = 32;
                        Vec3 start = projectile.position();
                        if (Math.abs(entity.getX() - projectile.getX()) > searchRange || Math.abs(entity.getY() - projectile.getY()) > searchRange || Math.abs(entity.getZ() - projectile.getZ()) > searchRange) {
                            continue;
                        }
                        Vec3 end = Utils.raycastForBlock(event.getLevel(), start, projectile.getDeltaMovement().normalize().scale(searchRange).add(start), ClipContext.Fluid.NONE).getLocation();
                        if (Utils.checkEntityIntersecting(entity, start, end, 3f).getType() == HitResult.Type.ENTITY) {
                            entry.getValue().add(projectile);
                        }
                    }
                }
            }
        }
    }

    private static final int tickDelay = 3;

    @SubscribeEvent
    public static void serverTick(LivingEvent.LivingTickEvent event) {
        if (TRACKED_ENTITIES.isEmpty()) {
            return;
        }
        var livingEntity = event.getEntity();
        if (livingEntity.tickCount % tickDelay == 0) {
            var projectiles = TRACKED_ENTITIES.get(event.getEntity().getUUID());
            if (projectiles != null) {
                if (livingEntity.isRemoved() || livingEntity.isDeadOrDying()) {
                    stopTracking(livingEntity);
                    return;
                }
                for (Projectile projectile : projectiles) {
                    float speed = (float) projectile.getDeltaMovement().length();
                    Vec3 magnetization = livingEntity.getBoundingBox().getCenter().subtract(projectile.position()).normalize().scale(speed * .3f);
                    projectile.setDeltaMovement(projectile.getDeltaMovement().add(magnetization));
                }
            }
        }
    }

    public static void startTracking(LivingEntity entity) {
        if (!TRACKED_ENTITIES.containsKey(entity.getUUID())) {
            TRACKED_ENTITIES.put(entity.getUUID(), new ArrayList<>());
        }
    }

    public static void stopTracking(LivingEntity entity) {
        TRACKED_ENTITIES.remove(entity.getUUID());
    }
}
