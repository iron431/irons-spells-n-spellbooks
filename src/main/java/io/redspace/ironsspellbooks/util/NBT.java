package io.redspace.ironsspellbooks.util;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class NBT {
    public static GlobalPos readGlobalPos(CompoundTag compoundTag) {
        var resourcelocation = new ResourceLocation(compoundTag.getString("res"));
        var posTag = (CompoundTag) compoundTag.get("pos");
        var blockPos = NbtUtils.readBlockPos(posTag);
        var resourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourcelocation);
        return GlobalPos.of(resourceKey, blockPos);
    }

    public static CompoundTag writeGlobalPos(GlobalPos globalPos) {
        var tag = new CompoundTag();
        tag.putString("res", globalPos.dimension().location().toString());

        var posTag = NbtUtils.writeBlockPos(globalPos.pos());
        tag.put("pos", posTag);

        return tag;
    }
}
