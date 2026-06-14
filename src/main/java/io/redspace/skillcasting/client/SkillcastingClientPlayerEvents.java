package io.redspace.skillcasting.client;

import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.network.ServerboundCancelSkillCastPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkillcastingClientPlayerEvents {
    private SkillcastingClientPlayerEvents() {
    }

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
        CastType castType = data.getActiveCastType();
        PacketDistributor.sendToServer(new ServerboundCancelSkillCastPacket());
    }
}
