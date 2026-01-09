package io.redspace.ironsspellbooks.patreon.statue;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeanShiftCluster {
    public static Vec3 rgbIntToOKLab(int rgb) {
        // Extract 8-bit components
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;
        r = Math.sqrt(r);
        g = Math.sqrt(g);
        b = Math.sqrt(b);

        // sRGB → linear RGB
        r = (r <= 0.04045) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.04045) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.04045) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        // Linear RGB → LMS
        double l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b;
        double m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b;
        double s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b;

        // Nonlinear transform
        l = Math.cbrt(l);
        m = Math.cbrt(m);
        s = Math.cbrt(s);

        // LMS → OKLab
        double L = 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s;
        double A = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s;
        double B = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s;

        return new Vec3(L, A, B);
    }

    public static int oklabToRgbInt(Vec3 lab) {
        double L = lab.x;
        double A = lab.y;
        double B = lab.z;

        // OKLab → LMS
        double l = L + 0.3963377774 * A + 0.2158037573 * B;
        double m = L - 0.1055613458 * A - 0.0638541728 * B;
        double s = L - 0.0894841775 * A - 1.2914855480 * B;

        // Cube
        l = l * l * l;
        m = m * m * m;
        s = s * s * s;

        // LMS → linear RGB
        double r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s;
        double g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s;
        double b = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s;

        // Linear RGB → sRGB
        r = (r <= 0.0031308) ? 12.92 * r : 1.055 * Math.pow(r, 1.0 / 2.4) - 0.055;
        g = (g <= 0.0031308) ? 12.92 * g : 1.055 * Math.pow(g, 1.0 / 2.4) - 0.055;
        b = (b <= 0.0031308) ? 12.92 * b : 1.055 * Math.pow(b, 1.0 / 2.4) - 0.055;
        r *= r;
        g *= g;
        b *= b;

        // Clamp and pack
        int ri = (int) Math.round(Math.min(1.0, Math.max(0.0, r)) * 255);
        int gi = (int) Math.round(Math.min(1.0, Math.max(0.0, g)) * 255);
        int bi = (int) Math.round(Math.min(1.0, Math.max(0.0, b)) * 255);

        return (0xFF << 24) + (ri << 16) + (gi << 8) + bi;
    }

    public static MeanShiftClusterResult meanShiftCluster(List<Integer> colorsI) {
        double bandwidth = 10 / 255.0;         // color similarity radius
        double bandwidthSqr = bandwidth * bandwidth;
        double merge = 1 / 255.0;
        double mergeSqr = merge * merge;
        double epsilon = 0.5 / 255.0;            // convergence threshold
        int maxIterations = 50;

        //todo: fastutil
        List<Vec3> clusters = new ArrayList<>(); // oklabspace
        List<Vec3> colors = new ArrayList<>(); // oklabspace
        HashMap<Integer, Color> lookupTable = new HashMap<>();

        for (int i : colorsI) {
            colors.add(rgbIntToOKLab(i));
        }
        for (int i = 0; i < colorsI.size(); i++) {
            int colorI = colorsI.get(i);
            Vec3 currentColor = colors.get(i);

            for (int j = 0; j < maxIterations; j++) {
                int count = 0;
                Vec3 sum = Vec3.ZERO;

                for (Vec3 color : colors) {
                    if (currentColor.distanceToSqr(color) < bandwidthSqr) {
                        sum = sum.add(color);
                        count++;
                    }
                }
                if (count == 0) {
                    break;
                }
                Vec3 average = sum.scale(1.0 / count);
                if (currentColor.distanceToSqr(average) < epsilon * epsilon) {
                    break;
                }
                currentColor = average;
            }
            boolean unique = true;
            Vec3 assignment = currentColor;
            for (Vec3 existingCluster : clusters) {
                if (currentColor.distanceToSqr(existingCluster) < mergeSqr) {
                    unique = false;
                    assignment = existingCluster;
                    break;
                }
            }
            if (unique) {
                clusters.add(currentColor);
            }
            lookupTable.put(colorI, new Color(oklabToRgbInt(assignment)));
        }
        List<Color> clustersI = new ArrayList<>(clusters.size());
        for (Vec3 oklab : clusters) {
            clustersI.add(new Color(oklabToRgbInt(oklab)));
        }
        return new MeanShiftClusterResult(clustersI, lookupTable);
    }

    public record MeanShiftClusterResult(List<Color> clusters, Map<Integer, Color> lookupTable) {
    }
}
