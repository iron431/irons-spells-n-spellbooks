package com.example.testmod.util;

import java.util.Arrays;

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

    public static String TimeFromTicks(float ticks, int decimalPlaces) {
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

    private static String stringTruncation(float f, int places) {
        int whole = (int) f;
        if (f % 1 == 0) {
            return ("" + whole);
        }
        String s = "" + f;
        int decimalIndex = s.indexOf(".");
        return whole + s.substring(decimalIndex, Math.min(decimalIndex + places + 1, s.length()));
    }
}
