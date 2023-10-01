package io.redspace.ironsspellbooks.util;

import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class CodeTimer {
    private final List<Tuple<String, Long>> timing = new ArrayList<>();

    public CodeTimer() {
        add("START");
    }

    public void add(String name) {
        timing.add(new Tuple<>(name, System.nanoTime()));
    }

    public String getOutput(String delimiter) {
        StringBuilder sb = new StringBuilder();

        long itemDelta = 0;
        long totalDelta = 0;

        for (int i = 0; i < timing.size(); i++) {
            var item = timing.get(i);

            if (i > 0) {
                var lastItem = timing.get(i - 1);
                itemDelta = item.getB() - lastItem.getB();
                totalDelta += itemDelta;
                sb.append(String.format("%s%s%s%s%f%s%f\n", lastItem.getA(), delimiter, item.getA(), delimiter, (itemDelta / 1000000d), delimiter, totalDelta / 1000000d));
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getOutput("\t");
    }
}
