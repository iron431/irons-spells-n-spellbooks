package com.example.testmod.util;

import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.UUID;

public class Utils {
    public static String GetStackTraceAsString() {
        var trace = Arrays.stream(Thread.currentThread().getStackTrace());
        StringBuffer sb = new StringBuffer();
        trace.forEach(item -> {
            sb.append(item.toString());
            sb.append("\n");
        });
        return sb.toString();
    }

    public static String timeFromTicks(float ticks, int decimalPlaces) {
        float ticks_to_seconds = 20;
        float seconds_to_minutes = 60;
        String affix = "s";
        float time = ticks / ticks_to_seconds;
        if (time > seconds_to_minutes) {
            time /= seconds_to_minutes;
            affix = "m";
        }
        return stringTruncation(time, decimalPlaces) + affix;
    }
    public static String decimalToPercent(double decimal){
        return stringTruncation(decimal,2)+"%";
    }

    public static boolean isPlayerHoldingSpellBook(Player player){
        return player.getMainHandItem().getItem() instanceof SpellBook || player.getOffhandItem().getItem() instanceof SpellBook;
    }

    public static boolean isPlayerHoldingScroll(Player player){
        return player.getMainHandItem().getItem() instanceof Scroll || player.getOffhandItem().getItem() instanceof Scroll;
    }

    public static ServerPlayer getServerPlayer(Level level, UUID uuid) {
        return level.getServer().getPlayerList().getPlayer(uuid);
    }

    public static String stringTruncation(double f, int places) {
        int whole = (int) f;
        if (f % 1 == 0) {
            return ("" + whole);
        }
        String s = "" + f;
        int decimalIndex = s.indexOf(".");
        return whole + s.substring(decimalIndex, Math.min(decimalIndex + places + 1, s.length()));
    }

    public static float getAngle(Vec2 a, Vec2 b)
    {
        return (float)(Math.atan2(b.y - a.y , b.x - a.x)) + 3.141f;// + (a.x > b.x ? Math.PI : 0));
    }

}
