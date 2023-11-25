package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class FixTetra extends DataFixerElement {

    private final String key = "sword/socket_material";

    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(key);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (!ModList.get().isLoaded("tetra")) {
            return false;
        } else if (tag != null) {
            if (tag.contains(key)) {
                var socketTag = tag.get(key);
                String poison = "sword_socket/irons_spellbooks_poison_rune_socket";
                String nature = "sword_socket/irons_spellbooks_nature_rune_socket";
                if (socketTag instanceof StringTag entry && entry.getAsString().equals(poison)) {
                    tag.remove(key);
                    tag.putString(key, nature);
                    return true;
                }
            }
        }
        return false;
    }
}
