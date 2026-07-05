package io.redspace.skillcasting.client;

import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.client.Minecraft;

public final class ClientSkillCastHelper {
    private static boolean suppressRightClicks;

    public static boolean shouldSuppressRightClicks() {
        return suppressRightClicks;
    }

    public static void setSuppressRightClicks(boolean suppress) {
        suppressRightClicks = suppress;
    }

    public static void stopSound(PlayableSound playableSound){
        Minecraft.getInstance().getSoundManager().stop(playableSound.soundEventHolder().value().getLocation(), null);
    }
}
