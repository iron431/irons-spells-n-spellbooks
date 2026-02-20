package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record StatueData(UUID uuid, PlayerStatuePose pose) {
    public static final Codec<StatueData> CODEC = RecordCodecBuilder.create(builder->builder.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(StatueData::uuid),
            PlayerStatuePose.CODEC.fieldOf("pose").forGetter(StatueData::pose)
    ).apply(builder,StatueData::new));

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof StatueData data && data.uuid.equals(this.uuid);
    }
}
