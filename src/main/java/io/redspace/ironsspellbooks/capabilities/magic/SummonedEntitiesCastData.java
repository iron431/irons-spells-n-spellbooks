package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SummonedEntitiesCastData implements ICastDataSerializable {
    protected Set<UUID> summons;
    protected float maxHealthPool;

    public SummonedEntitiesCastData() {
        this.summons = new HashSet<>();
    }

    public void add(Entity entity) {
        summons.add(entity.getUUID());
        if (entity instanceof LivingEntity livingEntity) {
            maxHealthPool += livingEntity.getMaxHealth();
        }
    }

    public void handleRemove(UUID uuid, MagicData ownerData, RecastInstance recastInstance) {
        summons.remove(uuid);
        if (summons.isEmpty()) {
            ownerData.getPlayerRecasts().removeRecast(recastInstance, RecastResult.USED_ALL_RECASTS);
        }
    }

    @Override
    public void reset() {
    }

    public float getMaxHealthPool() {
        return maxHealthPool;
    }

    public Set<UUID> getSummons() {
        return summons;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(summons.size());
        for (var uuid : summons) {
            buffer.writeUUID(uuid);
        }
        buffer.writeFloat(maxHealthPool);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        int i = buffer.readInt();
        for (int j = 0; j < i; j++) {
            summons.add(buffer.readUUID());
        }
        this.maxHealthPool = buffer.readFloat();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        summons.forEach(uuid -> list.add(NbtUtils.createUUID(uuid)));
        tag.put("summons", list);
        tag.putFloat("maxHealthPool", maxHealthPool);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag list = nbt.getList("summons", IntArrayTag.TAG_INT_ARRAY);
        list.forEach(tag -> summons.add(NbtUtils.loadUUID(tag)));
        this.maxHealthPool = nbt.getFloat("maxHealthPool");
    }
}