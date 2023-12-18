package io.redspace.ironsspellbooks.effect.guiding_bolt;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.datafix.IronsSpellBooksWorldData;
import io.redspace.ironsspellbooks.internal_event.IronsWorldDatEvent;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class GuidingBoltManager {

    public static final HashMap<UUID, ArrayList<Projectile>> TRACKED_ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void onProjectileShot(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getEntity() instanceof Projectile projectile) {
                Vec3 start = projectile.position();
                int searchRange = 32;
                Vec3 end = Utils.raycastForBlock(event.getLevel(), start, projectile.getDeltaMovement().normalize().scale(searchRange).add(start), ClipContext.Fluid.NONE).getLocation();
                for (Map.Entry<UUID, ArrayList<Projectile>> entry : TRACKED_ENTITIES.entrySet()) {
                    var entity = serverLevel.getEntity(entry.getKey());
                    if (entity != null) {
                        if (Math.abs(entity.getX() - projectile.getX()) > searchRange || Math.abs(entity.getY() - projectile.getY()) > searchRange || Math.abs(entity.getZ() - projectile.getZ()) > searchRange) {
                            continue;
                        }
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
    public static void livingTick(LivingEvent.LivingTickEvent event) {
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

//    @SubscribeEvent
//    public static void readFromWorldDat(IronsWorldDatEvent.Load event) {
//        CompoundTag tag = event.getTag();
//        deserialize(tag);
//        IronsSpellbooks.LOGGER.debug("GuidingBoltManager.readFromWorldDat map before:{}", TRACKED_ENTITIES);
//        IronsSpellbooks.LOGGER.debug("GuidingBoltManager.readFromWorldDat tag:{}", tag);
//
//        IronsSpellbooks.LOGGER.debug("GuidingBoltManager.readFromWorldDat map after:{}", TRACKED_ENTITIES);
//
//    }

//    @SubscribeEvent
//    public static void saveToWorldDat(IronsWorldDatEvent.Save event) {
//        if (!TRACKED_ENTITIES.isEmpty()) {
//            serialize(event.getTag());
//            IronsSpellbooks.LOGGER.debug("GuidingBoltManager.saveToWorldDat tag:{}", event.getTag());
//        }
//    }

    public static void serialize(CompoundTag tag) {
        ListTag uuids = new ListTag();
        for (UUID key : TRACKED_ENTITIES.keySet()) {
            uuids.add(NbtUtils.createUUID(key));
        }
        tag.put("GuidingBoltManager", uuids);
    }

    public static void deserialize(CompoundTag tag) {
        if (tag.contains("GuidingBoltManager", 9)) {
            ListTag list = tag.getList("GuidingBoltManager", 11);
            for (Tag uuidTag : list) {
                try {
                    var uuid = NbtUtils.loadUUID(uuidTag);
                    TRACKED_ENTITIES.put(uuid, new ArrayList<>());
                } catch (Exception ignored) {
                    continue;
                }
            }
        }
    }

    public static void startTracking(LivingEntity entity) {
        if (!TRACKED_ENTITIES.containsKey(entity.getUUID())) {
            TRACKED_ENTITIES.put(entity.getUUID(), new ArrayList<>());
        }
        IronsSpellBooksWorldData.INSTANCE.guidingBoltManager = TRACKED_ENTITIES;
    }

    public static void stopTracking(LivingEntity entity) {
        TRACKED_ENTITIES.remove(entity.getUUID());
        IronsSpellBooksWorldData.INSTANCE.guidingBoltManager = TRACKED_ENTITIES;
    }
}
