package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.network.SyncAllCameraShakesPacket;
import io.redspace.ironsspellbooks.network.SyncCameraShakePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class CameraShakeManager {
    public static final ArrayList<CameraShakeData> cameraShakeData = new ArrayList<>();
    public static ArrayList<CameraShakeData> clientCameraShakeData = new ArrayList<>();
    //    private static final int tickDelay = 5;
    private static int nextId = 0;

    public static int getNextId() {
        return nextId++;
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        IronsSpellbooks.LOGGER.debug("camerahsake server tick");
        if (cameraShakeData.isEmpty()) {
            return;
        }
//        int ticks = event.getServer().getTickCount();
//        if (ticks % tickDelay == 0) {
        //fixme: this is not tracked per-dimension
        ArrayList<CameraShakeData> complete = new ArrayList<>();
        for (CameraShakeData data : cameraShakeData) {
            data.tickCount++;
            //IronsSpellbooks.LOGGER.debug("{}/{}", data.tickCount, data.duration);
            if (data.tickCount >= data.duration) {
                complete.add(data);
            }
        }
        if (!complete.isEmpty()) {
            //IronsSpellbooks.LOGGER.debug("CameraShakeManager.onWorldTick: removing complete data");
            complete.forEach(CameraShakeManager::removeCameraShake);
        }
//        }
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


//    private static void doSync() {
//        PacketDistributor.sendToAllPlayers(new SyncCameraShakePacket(cameraShakeData));
//    }

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
        List<CameraShakeData> closestCameraShakes = clientCameraShakeData.stream().sorted((o1, o2) -> o1.origin.distanceToSqr(player.position()) < o2.origin.distanceToSqr(player.position()) ? -1 : 1).toList();
        var cameraShake = closestCameraShakes.get(0);
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
        IronsSpellbooks.LOGGER.debug("camerahsake client tick");
//        if (cameraShakeData.isEmpty()) {
//            return;
//        }
        if (Minecraft.getInstance().isSingleplayer() && Minecraft.getInstance().isPaused()) {
            return;
        }
        for (var data : clientCameraShakeData) {
            data.tickCount++;
        }
    }
}
