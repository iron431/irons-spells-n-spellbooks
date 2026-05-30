package io.redspace.skillcasting.lifecycle;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.skillcasting.client.SkillWheelOverlay;
import io.redspace.skillcasting.network.CastInputPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Default client keybinds for the skillcasting HUD prototype (V = cast, hold R = skill wheel).
 */
public final class SkillcastingClientEvents {
    private SkillcastingClientEvents() {
    }

    @SubscribeEvent
    public static void onKeypress(InputEvent.Key event) {
        if (event.getKey() == InputConstants.KEY_V && event.getAction() == InputConstants.PRESS) {
            PacketDistributor.sendToServer(new CastInputPacket(SkillcastingData.get(Minecraft.getInstance().player).isCasting() ? CastInputPacket.Action.CANCEL : CastInputPacket.Action.CAST, "", 1));
        }
        if (event.getKey() == InputConstants.KEY_R) {
            if (Minecraft.getInstance().screen == null) {
                if (event.getAction() == InputConstants.PRESS) {
                    SkillWheelOverlay.instance.open();
                } else if (event.getAction() == InputConstants.RELEASE && SkillWheelOverlay.instance.active) {
                    SkillWheelOverlay.instance.close();
                }
            }
        }
    }
}
