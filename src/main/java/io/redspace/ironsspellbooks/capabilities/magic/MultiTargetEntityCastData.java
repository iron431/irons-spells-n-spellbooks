package io.redspace.ironsspellbooks.capabilities.magic;

import com.mojang.serialization.Codec;
import io.redspace.skillcastingapi.data.ICastDataSerializable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MultiTargetEntityCastData implements ICastDataSerializable<MultiTargetEntityCastData> {
    private List<UUID> targetUUIDs;

    public MultiTargetEntityCastData(Entity... targets) {
        this.targetUUIDs = new ArrayList<>();
        Arrays.stream(targets).forEach(target -> targetUUIDs.add(target.getUUID()));
    }

    private MultiTargetEntityCastData(List<UUID> targets) {
        this.targetUUIDs = new ArrayList<>();
        targetUUIDs.addAll(targets);
    }

    @Override
    public void reset() {
        targetUUIDs.clear();
    }

    public List<UUID> getTargets() {
        return targetUUIDs;
    }

    public void addTarget(Entity entity) {
        this.targetUUIDs.add(entity.getUUID());
    }

    public void addTarget(UUID uuid) {
        this.targetUUIDs.add(uuid);
    }

    public boolean isTargeted(Entity entity) {
        return targetUUIDs.contains(entity.getUUID());
    }

    public static void writeToBuffer(FriendlyByteBuf buffer, MultiTargetEntityCastData data) {
        buffer.writeInt(data.targetUUIDs.size());
        data.targetUUIDs.forEach(buffer::writeUUID);
    }

    public static MultiTargetEntityCastData readFromBuffer(FriendlyByteBuf buffer) {
        MultiTargetEntityCastData data = new MultiTargetEntityCastData();
        data.targetUUIDs = new ArrayList<>();
        int i = buffer.readInt();
        for (int j = 0; j < i; j++) {
            data.targetUUIDs.add(buffer.readUUID());
        }
        return data;
    }

//    @Override
//    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
//        CompoundTag tag = new CompoundTag();
//        ListTag uuids = new ListTag();
//        targetUUIDs.stream().map(NbtUtils::createUUID).forEach(uuids::add);
//        tag.put("targets", uuids);
//        return tag;
//    }
//
//    @Override
//    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
//        targetUUIDs = new ArrayList<>();
//        ListTag listTag = nbt.getList("targets", 11);
//        listTag.stream().map(NbtUtils::loadUUID).forEach(targetUUIDs::add);
//    }

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiTargetEntityCastData> STREAM_CODEC = StreamCodec.of(MultiTargetEntityCastData::writeToBuffer, MultiTargetEntityCastData::readFromBuffer);

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MultiTargetEntityCastData> streamCodec() {
        return STREAM_CODEC;
    }

    public static final Codec<MultiTargetEntityCastData> CODEC = Codec.list(UUIDUtil.CODEC).xmap(MultiTargetEntityCastData::new, MultiTargetEntityCastData::getTargets);

    @Override
    public Codec<MultiTargetEntityCastData> codec() {
        return CODEC;
    }
}