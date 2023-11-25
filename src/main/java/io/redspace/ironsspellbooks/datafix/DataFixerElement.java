package io.redspace.ironsspellbooks.datafix;

import net.minecraft.nbt.CompoundTag;

import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class DataFixerElement {
    private List<byte[]> preScanData;

    final public List<byte[]> preScanValueBytes() {
        if (preScanData == null) {
            preScanData = preScanValuesToMatch().stream().map(item -> {
                return item.getBytes(StandardCharsets.UTF_8);
            }).toList();
        }

        return preScanData;
    }

    /**
     * If the value returned is found in the chunk that chunk will be processed. If not the chunk will be skipped.
     */
    public abstract List<String> preScanValuesToMatch();

    /**
     * Return true if any data in the tag was modified
     */
    public abstract boolean runFixer(CompoundTag tag);
}
