package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.network.SyncCameraShakePacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber
public class CameraShakeManager {
    public static final ArrayList<CameraShakeData> cameraShakeData = new ArrayList<>();
    public static ArrayList<CameraShakeData> clientCameraShakeData = new ArrayList<>();
    private static int nextId = 0;

    public static int getNextId() {
        return nextId++;
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (cameraShakeData.isEmpty() || event.phase == TickEvent.Phase.START) {
            return;
        }
        //fixme: this is not tracked per-dimension
        ArrayList<CameraShakeData> completed = new ArrayList<>();
        for (CameraShakeData data : cameraShakeData) {
            data.tickCount++;
            if (data.tickCount >= data.duration) {
                completed.add(data);
            }
        }
        if (!completed.isEmpty()) {
            completed.forEach(CameraShakeManager::removeCameraShake);
        }
    }

    public static void addCameraShake(CameraShakeData data) {
        cameraShakeData.add(data);
        PacketDistributor.sendToAllPlayers(new SyncCameraShakePacket(data, false));
    }

    public static void removeCameraShake(CameraShakeData data) {
        if (cameraShakeData.removeIf(instance -> instance.id == data.id)) {
            PacketDistributor.sendToAllPlayers(new SyncCameraShakePacket(data, true));
        }
    }

    public static void addClientCameraShake(CameraShakeData data) {
        clientCameraShakeData.add(data);
    }

    public static void removeClientCameraShake(CameraShakeData data) {
        clientCameraShakeData.removeIf(instance -> instance.id == data.id);
    }

    public static void doSync(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncAllCameraShakesPacket(cameraShakeData));
    }

    private static final int fadeoutDuration = 20;
    private static final float fadeoutMultiplier = 1f / fadeoutDuration;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void handleCameraShake(ViewportEvent.ComputeCameraAngles event) {
        if (clientCameraShakeData.isEmpty()) {
            return;
        }

        var player = event.getCamera().getEntity();
        List<CameraShakeData> sortedActiveCameraShakes = clientCameraShakeData.stream()
                .filter(data -> data.dimension.equals(player.level.dimension()))
                .sorted(Comparator.comparingDouble(o -> o.origin.distanceToSqr(player.position())))
                .toList();
        if (sortedActiveCameraShakes.isEmpty()) {
            return;
        }
        var cameraShake = sortedActiveCameraShakes.get(0);
        var closestPos = cameraShake.origin;

        float distanceMultiplier = 1 / (cameraShake.radius * cameraShake.radius);
        float fadeout = (cameraShake.duration - cameraShake.tickCount) >= fadeoutDuration ? 1f
                : ((cameraShake.duration - cameraShake.tickCount) * fadeoutMultiplier);
        float intensity = (float) Mth.clampedLerp(1, 0, closestPos.distanceToSqr(player.position()) * distanceMultiplier) * fadeout;

        float f = (float) (player.tickCount + event.getPartialTick());
        float yaw = Mth.cos(f * 1.5f) * intensity * .5f;
        float pitch = Mth.cos(f * 2f) * intensity * .5f;
        float roll = Mth.sin(f * 2.2f) * intensity * .5f;
        event.setYaw(event.getYaw() + yaw);
        event.setRoll(event.getRoll() + roll);
        event.setPitch(event.getPitch() + pitch);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void handleCameraShake(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isSingleplayer() && Minecraft.getInstance().isPaused()) {
            return;
        }
        for (var data : clientCameraShakeData) {
            data.tickCount++;
        }
    }
}
