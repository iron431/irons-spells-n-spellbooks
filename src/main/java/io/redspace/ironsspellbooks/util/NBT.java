package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class NBT {
    public static GlobalPos readGlobalPos(CompoundTag compoundTag) {
        var resourcelocation = ResourceLocation.parse(compoundTag.getString("res"));
        var posTag = (CompoundTag) compoundTag.get("pos");
        var blockPos = NbtUtils.readBlockPos(posTag);
        var resourceKey = ResourceKey.create(Registries.DIMENSION, resourcelocation);
        return GlobalPos.of(resourceKey, blockPos);
    }

    public static CompoundTag writeGlobalPos(GlobalPos globalPos) {
        var tag = new CompoundTag();
        tag.putString("res", globalPos.dimension().location().toString());

        var posTag = NbtUtils.writeBlockPos(globalPos.pos());
        tag.put("pos", posTag);

        return tag;
    }

    public static CompoundTag writePortalPos(PortalPos globalPos) {
        var tag = new CompoundTag();
        tag.putString("res", globalPos.dimension().location().toString());

        var posTag = writeVec3Pos(globalPos.pos());
        tag.put("pos", posTag);

        tag.putFloat("rot", globalPos.rotation());

        return tag;
    }

    public static PortalPos readPortalPos(CompoundTag compoundTag) {
        var resourcelocation = ResourceLocation.parse(compoundTag.getString("res"));
        var posTag = (CompoundTag) compoundTag.get("pos");
        var pos = readVec3(posTag);
        var resourceKey = ResourceKey.create(Registries.DIMENSION, resourcelocation);
        var rotation = compoundTag.getFloat("rot");
        return PortalPos.of(resourceKey, pos, rotation);
    }

    public static Vec3 readVec3(CompoundTag pTag) {
        return new Vec3(pTag.getDouble("X"), pTag.getDouble("Y"), pTag.getDouble("Z"));
    }

    public static CompoundTag writeVec3Pos(Vec3 pPos) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putDouble("X", pPos.x);
        compoundtag.putDouble("Y", pPos.y);
        compoundtag.putDouble("Z", pPos.z);
        return compoundtag;
    }
}
