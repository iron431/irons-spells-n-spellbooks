package io.redspace.ironsspellbooks.effect.guiding_bolt;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.network.ClientboundGuidingBoltManagerStartTracking;
import io.redspace.ironsspellbooks.network.ClientboundGuidingBoltManagerStopTracking;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.openjdk.nashorn.internal.ir.EmptyNode;

import java.util.*;

@Mod.EventBusSubscriber
public class GuidingBoltManager implements INBTSerializable<CompoundTag> {

    public static final GuidingBoltManager INSTANCE = new GuidingBoltManager();
    private final HashMap<UUID, ArrayList<Projectile>> trackedEntities = new HashMap<>();
    private final HashMap<ResourceKey<Level>, List<Projectile>> dirtyProjectiles = new HashMap<>();
    private final int tickDelay = 3;

    public void startTracking(LivingEntity entity) {
        if (!entity.level.isClientSide) {
            if (!trackedEntities.containsKey(entity.getUUID())) {
                trackedEntities.put(entity.getUUID(), new ArrayList<>());
                IronsDataStorage.INSTANCE.setDirty();
            }
        }
    }

    public void stopTracking(LivingEntity entity) {
        if (!entity.level.isClientSide) {
            trackedEntities.remove(entity.getUUID());
            IronsDataStorage.INSTANCE.setDirty();
            Messages.sendToPlayersTrackingEntity(new ClientboundGuidingBoltManagerStopTracking(entity), entity);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        ListTag uuids = new ListTag();
        for (UUID key : trackedEntities.keySet()) {
            uuids.add(NbtUtils.createUUID(key));
        }
        tag.put("TrackedEntities", uuids);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        ListTag list = compoundTag.getList("TrackedEntities", 11);
        for (Tag uuidTag : list) {
            try {
                var uuid = NbtUtils.loadUUID(uuidTag);
                trackedEntities.put(uuid, new ArrayList<>());
            } catch (Exception ignored) {
                continue;
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileShot(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (!INSTANCE.trackedEntities.isEmpty() && event.getEntity() instanceof Projectile projectile) {
                INSTANCE.dirtyProjectiles.computeIfAbsent(serverLevel.dimension(), (key) -> new ArrayList<>()).add(projectile);
            }
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START || INSTANCE.dirtyProjectiles.isEmpty()) {
            return;
        }
        if (event.level instanceof ServerLevel serverLevel) {
            HashMap<Entity, List<Projectile>> toSync = new HashMap<Entity, List<Projectile>>();
            var dirtyProjectiles = INSTANCE.dirtyProjectiles.getOrDefault(serverLevel.dimension(), List.of());
            for (int i = dirtyProjectiles.size() - 1; i >= 0; i--) {
                var projectile = dirtyProjectiles.get(i);
                if (projectile.isAddedToWorld()) {
                    Vec3 start = projectile.position();
                    int searchRange = 48;
                    Vec3 end = Utils.raycastForBlock(serverLevel, start, projectile.getDeltaMovement().normalize().scale(searchRange).add(start), ClipContext.Fluid.NONE).getLocation();
                    for (Map.Entry<UUID, ArrayList<Projectile>> entityToTrackedProjectiles : GuidingBoltManager.INSTANCE.trackedEntities.entrySet()) {
                        var entity = serverLevel.getEntity(entityToTrackedProjectiles.getKey());
                        if (entity != null) {
                            if (Math.abs(entity.getX() - projectile.getX()) > searchRange || Math.abs(entity.getY() - projectile.getY()) > searchRange || Math.abs(entity.getZ() - projectile.getZ()) > searchRange) {
                                continue;
                            }
                            if (Utils.checkEntityIntersecting(entity, start, end, 3.5f + Math.min(entity.getBbWidth() * .5f, 2)).getType() == HitResult.Type.ENTITY) {
                                updateTrackedProjectiles(entityToTrackedProjectiles.getValue(), projectile);
                                toSync.computeIfAbsent(entity, (key) -> new ArrayList<>()).add(projectile);
                                break;
                            }
                        }
                    }
                    dirtyProjectiles.remove(i);
                }
            }
            for (Map.Entry<Entity, List<Projectile>> entry : toSync.entrySet()) {
                var entity = entry.getKey();
                Messages.sendToPlayersTrackingEntity(new ClientboundGuidingBoltManagerStartTracking(entity, entry.getValue()), entity);
            }
        }
    }

    private static void updateTrackedProjectiles(List<Projectile> tracked, Projectile toTrack) {
        updateTrackedProjectiles(tracked, List.of(toTrack));
    }

    private static void updateTrackedProjectiles(List<Projectile> tracked, List<Projectile> toTrack) {
        tracked.removeIf(Entity::isRemoved);
        tracked.addAll(toTrack);
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event) {
//        if (MinecraftInstanceHelper.getPlayer() == event.getEntity() && event.getEntity().tickCount % 20 == 0) {
//            IronsSpellbooks.LOGGER.debug("\nGuiding Bolt Dump");
//            for (Map.Entry entry : GuidingBoltManager.INSTANCE.trackedEntities.entrySet()) {
//                IronsSpellbooks.LOGGER.debug("{}: {}", entry.getKey(), entry.getValue());
//            }
//        }
        if (GuidingBoltManager.INSTANCE.trackedEntities.isEmpty()) {
            return;
        }
        var livingEntity = event.getEntity();
        if (livingEntity.tickCount % GuidingBoltManager.INSTANCE.tickDelay == 0) {
            var projectiles = GuidingBoltManager.INSTANCE.trackedEntities.get(event.getEntity().getUUID());
            if (projectiles != null) {
                if (livingEntity.isRemoved() || livingEntity.isDeadOrDying()) {
                    GuidingBoltManager.INSTANCE.stopTracking(livingEntity);
                    return;
                }
                List<Projectile> projectilesToRemove = new ArrayList<>();
                for (Projectile projectile : projectiles) {
                    Vec3 motion = projectile.getDeltaMovement();
                    float speed = (float) motion.length();
                    Vec3 home = livingEntity.getBoundingBox().getCenter().subtract(projectile.position()).normalize().scale(speed * .45f);
                    if (home.dot(motion) < 0) {
                        //We have passed the entity
                        projectilesToRemove.add(projectile);
                        continue;
                    }
                    Vec3 newMotion = motion.add(home).normalize().scale(speed);
                    projectile.setDeltaMovement(newMotion);
                }
                projectiles.removeAll(projectilesToRemove);
            }
        }
    }

    public static void handleClientboundStartTracking(UUID uuid, List<Integer> projectileIds) {
        var level = Minecraft.getInstance().level;
        List<Projectile> projectiles = new ArrayList<>();
        for (Integer i : projectileIds) {
            if (level.getEntity(i) instanceof Projectile projectile) {
                updateTrackedProjectiles(projectiles, projectile);
            }
        }
        INSTANCE.trackedEntities.computeIfAbsent(uuid, (key) -> new ArrayList<>()).addAll(projectiles);
    }

    public static void handleClientboundStopTracking(UUID uuid) {
        INSTANCE.trackedEntities.remove(uuid);
    }

    public static void handleClientLogout() {
        INSTANCE.trackedEntities.clear();
    }
}
