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
import java.util.List;

@EventBusSubscriber
public class CameraShakeManager {
    public static final ArrayList<CameraShakeData> cameraShakeData = new ArrayList<>();
    public static ArrayList<CameraShakeData> clientCameraShakeData = new ArrayList<>();
    private static final int tickDelay = 5;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (cameraShakeData.isEmpty() || event.phase == TickEvent.Phase.START) {
            return;
        }
        int ticks = event.getServer().getTickCount();
        if (ticks % tickDelay == 0) {
            ArrayList<CameraShakeData> complete = new ArrayList<>();
            for (CameraShakeData data : cameraShakeData) {
                data.tickCount += tickDelay;
                //IronsSpellbooks.LOGGER.debug("{}/{}", data.tickCount, data.duration);
                if (data.tickCount >= data.duration) {
                    complete.add(data);
                }
            }
            if (!complete.isEmpty()) {
                //IronsSpellbooks.LOGGER.debug("CameraShakeManager.onWorldTick: removing complete data");
                cameraShakeData.removeAll(complete);
                doSync();
            }
        }
    }

    public static void addCameraShake(CameraShakeData data) {
        cameraShakeData.add(data);
        doSync();
    }

    public static void removeCameraShake(CameraShakeData data) {
        if (cameraShakeData.remove(data)) {
            doSync();
        }
    }

    private static void doSync() {
        PacketDistributor.sendToAllPlayers(new SyncCameraShakePacket(cameraShakeData));
    }

    public static void doSync(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncCameraShakePacket(cameraShakeData));
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
        //Fixme: tick count is not kept track of on client, so this does nothing
        float fadeout = (cameraShake.duration - cameraShake.tickCount) > fadeoutDuration ? 1 : ((cameraShake.duration - cameraShake.tickCount) * fadeoutMultiplier);
        float intensity = (float) Mth.clampedLerp(1, 0, closestPos.distanceToSqr(player.position()) * distanceMultiplier) * fadeout;

        float f = (float) (player.tickCount + event.getPartialTick());
        float yaw = Mth.cos(f * 1.5f) * intensity * .35f;
        float pitch = Mth.cos(f * 2f) * intensity * .35f;
        float roll = Mth.sin(f * 2.2f) * intensity * .35f;
        event.setYaw(event.getYaw() + yaw);
        event.setRoll(event.getRoll() + roll);
        event.setPitch(event.getPitch() + pitch);
    }
}
