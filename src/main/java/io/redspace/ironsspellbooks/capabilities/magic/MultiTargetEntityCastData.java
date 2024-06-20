package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MultiTargetEntityCastData implements ICastDataSerializable {
    private List<UUID> targetUUIDs;

    public MultiTargetEntityCastData(LivingEntity... targets) {
        this.targetUUIDs = new ArrayList<>();
        Arrays.stream(targets).forEach(target -> targetUUIDs.add(target.getUUID()));
    }

    @Override
    public void reset() {
        targetUUIDs.clear();
    }

    public List<UUID> getTargets() {
        return targetUUIDs;
    }

    public void addTarget(LivingEntity entity) {
        this.targetUUIDs.add(entity.getUUID());
    }

    public void addTarget(UUID uuid) {
        this.targetUUIDs.add(uuid);
    }

    public boolean isTargeted(LivingEntity entity){
        return targetUUIDs.contains(entity.getUUID());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(targetUUIDs.size());
        targetUUIDs.forEach(buffer::writeUUID);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        targetUUIDs = new ArrayList<>();
        int i = buffer.readInt();
        for (int j = 0; j < i; j++) {
            targetUUIDs.add(buffer.readUUID());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag uuids = new ListTag();
        targetUUIDs.stream().map(NbtUtils::createUUID).forEach(uuids::add);
        tag.put("targets", uuids);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        targetUUIDs = new ArrayList<>();
        ListTag listTag = nbt.getList("targets", 11);
        listTag.stream().map(NbtUtils::loadUUID).forEach(targetUUIDs::add);
    }
}