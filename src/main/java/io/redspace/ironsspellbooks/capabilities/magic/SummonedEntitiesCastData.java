package io.redspace.ironsspellbooks.capabilities.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcastingapi.data.ICastDataSerializable;
import io.redspace.skillcastingapi.data.RecastInstance;
import io.redspace.skillcastingapi.data.SkillcastingData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SummonedEntitiesCastData implements ICastDataSerializable<SummonedEntitiesCastData> {
    protected Set<UUID> summons;
    protected float maxHealthPool;

    public SummonedEntitiesCastData() {
        this.summons = new HashSet<>();
    }

    private static SummonedEntitiesCastData fromDisk(List<UUID> summons, float healthPool) {
        var data = new SummonedEntitiesCastData();
        data.summons.addAll(summons);
        data.maxHealthPool = healthPool;
        return data;
    }

    public void add(Entity entity) {
        summons.add(entity.getUUID());
        if (entity instanceof LivingEntity livingEntity) {
            maxHealthPool += livingEntity.getMaxHealth();
        }
    }

    public void handleRemove(UUID uuid, SkillcastingData ownerData, RecastInstance recastInstance) {
        summons.remove(uuid);
        if (summons.isEmpty()) {
            //todo: don't we create unique cast context for recast ending?
            throw new NotImplementedException();
//            ownerData.getRecasts().removeRecast(recastInstance, RecastResult.USED_ALL_RECASTS);
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

    private List<UUID> listSummons() {
        return summons.stream().toList();
    }

    Codec<SummonedEntitiesCastData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(UUIDUtil.CODEC).fieldOf("summons").forGetter(SummonedEntitiesCastData::listSummons),
            Codec.FLOAT.fieldOf("healthPool").forGetter(SummonedEntitiesCastData::getMaxHealthPool)
    ).apply(builder, SummonedEntitiesCastData::fromDisk));

    StreamCodec<RegistryFriendlyByteBuf, SummonedEntitiesCastData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeInt(data.summons.size());
                for (var uuid : data.summons) {
                    buf.writeUUID(uuid);
                }
                buf.writeFloat(data.maxHealthPool);
            },
            (buf) -> {
                var data = new SummonedEntitiesCastData();
                int i = buf.readInt();
                for (int j = 0; j < i; j++) {
                    data.summons.add(buf.readUUID());
                }
                data.maxHealthPool = buf.readFloat();
                return data;
            }
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SummonedEntitiesCastData> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public Codec<SummonedEntitiesCastData> codec() {
        return CODEC;
    }
//    @Override
//    public void writeToBuffer(FriendlyByteBuf buffer) {
//        buffer.writeInt(summons.size());
//        for (var uuid : summons) {
//            buffer.writeUUID(uuid);
//        }
//        buffer.writeFloat(maxHealthPool);
//    }
//
//    @Override
//    public void readFromBuffer(FriendlyByteBuf buffer) {
//        int i = buffer.readInt();
//        for (int j = 0; j < i; j++) {
//            summons.add(buffer.readUUID());
//        }
//        this.maxHealthPool = buffer.readFloat();
//    }
//
//    @Override
//    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
//        CompoundTag tag = new CompoundTag();
//        ListTag list = new ListTag();
//        summons.forEach(uuid -> list.add(NbtUtils.createUUID(uuid)));
//        tag.put("summons", list);
//        tag.putFloat("maxHealthPool", maxHealthPool);
//        return tag;
//    }
//
//    @Override
//    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
//        ListTag list = nbt.getList("summons", IntArrayTag.TAG_INT_ARRAY);
//        list.forEach(tag -> summons.add(NbtUtils.loadUUID(tag)));
//        this.maxHealthPool = nbt.getFloat("maxHealthPool");
//    }
}