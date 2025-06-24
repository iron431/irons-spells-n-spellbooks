package io.redspace.ironsspellbooks.particle;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientShieldHelper {
    private static final ArrayList<AbstractShieldEntity> trackedEntities = new ArrayList<>();

    @SubscribeEvent
    public static void trackShieldCreated(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractShieldEntity ase) {
            trackedEntities.add(ase);
        }
    }

    @SubscribeEvent
    public static void trackShieldRemoved(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof AbstractShieldEntity ase) {
            trackedEntities.remove(ase);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        trackedEntities.clear();
    }

    public static List<VoxelShape> getShieldsFor(AABB boundingBox) {
        if (trackedEntities.isEmpty() || !ClientConfigs.SHIELD_PARTICLE_COLLISIONS.get()) {
            return List.of();
        } else {
            List<VoxelShape> shieldCollisions = new ArrayList<>();
            trackedEntities.forEach((s) -> {
                if (boundingBox.intersects(s.getBoundingBox().inflate(1))) shieldCollisions.addAll(s.getVoxels());
            });
            return shieldCollisions;
        }
    }
}
