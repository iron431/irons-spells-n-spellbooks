package io.redspace.ironsspellbooks.effect.guiding_bolt;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.data.IronsSpellBooksWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class GuidingBoltManager implements INBTSerializable<CompoundTag> {

    public static final GuidingBoltManager INSTANCE = new GuidingBoltManager();
    private final HashMap<UUID, ArrayList<Projectile>> trackedEntities = new HashMap<>();
    private final int tickDelay = 3;

    public void startTracking(LivingEntity entity) {
        if (!trackedEntities.containsKey(entity.getUUID())) {
            trackedEntities.put(entity.getUUID(), new ArrayList<>());
            IronsSpellBooksWorldData.INSTANCE.setDirty();
        }
    }

    public void stopTracking(LivingEntity entity) {
        trackedEntities.remove(entity.getUUID());
        IronsSpellBooksWorldData.INSTANCE.setDirty();
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
            if (event.getEntity() instanceof Projectile projectile) {
                Vec3 start = projectile.position();
                int searchRange = 32;
                Vec3 end = Utils.raycastForBlock(event.getLevel(), start, projectile.getDeltaMovement().normalize().scale(searchRange).add(start), ClipContext.Fluid.NONE).getLocation();
                for (Map.Entry<UUID, ArrayList<Projectile>> entry : GuidingBoltManager.INSTANCE.trackedEntities.entrySet()) {
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

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event) {
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
                for (Projectile projectile : projectiles) {
                    float speed = (float) projectile.getDeltaMovement().length();
                    Vec3 magnetization = livingEntity.getBoundingBox().getCenter().subtract(projectile.position()).normalize().scale(speed * .3f);
                    projectile.setDeltaMovement(projectile.getDeltaMovement().add(magnetization));
                }
            }
        }
    }
}
