package io.redspace.skillcasting.client;

import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.network.ServerboundCancelSkillCastPacket;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkillcastingClientPlayerEvents {

    @SubscribeEvent
    public static void onPlayerOpenScreen(ScreenEvent.Opening event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        SkillcastingData data = SkillcastingData.get(player);
        if (!data.isCasting()) {
            return;
        }
        PacketDistributor.sendToServer(new ServerboundCancelSkillCastPacket());
    }

    @SubscribeEvent
    public static void onCalculatePlayerSpeed(MovementInputUpdateEvent event) {
        if (SkillcastingData.get(event.getEntity()).isCasting()) {
            ActiveCast cast = SkillcastingData.get(event.getEntity()).getActiveCast();
            float speed = Mth.clamp(cast.context().getOrDefault(SkillcastingComponentTypes.CASTING_MOVESPEED_MULTIPLIER, 1f),
                    0, 1);
            event.getInput().forwardImpulse *= speed;
            event.getInput().leftImpulse *= speed;
        }
    }

}
