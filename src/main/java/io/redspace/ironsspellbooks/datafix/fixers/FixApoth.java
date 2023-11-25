package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class FixApoth extends DataFixerElement {
    private final String key = "affix_data";

    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(key);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (!ModList.get().isLoaded("apotheosis")) {
            return false;
        } else if (tag != null) {
            if (tag.get(key) instanceof CompoundTag affixTag) {
                if (affixTag.get("gems") instanceof ListTag gemList) {
                    for (Tag gem : gemList) {
                        var itemTag = ((CompoundTag) gem).getCompound("tag");
                        if (itemTag.getString("gem").equals("irons_spellbooks:poison")) {
                            itemTag.putString("gem", "irons_spellbooks:nature");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
