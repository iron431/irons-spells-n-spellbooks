package io.redspace.skillcasting.client;

public final class ClientSkillCastHelper {
    private static boolean suppressRightClicks;

    private ClientSkillCastHelper() {
    }

    public static boolean shouldSuppressRightClicks() {
        return suppressRightClicks;
    }

    public static void setSuppressRightClicks(boolean suppress) {
        suppressRightClicks = suppress;
    }
}
