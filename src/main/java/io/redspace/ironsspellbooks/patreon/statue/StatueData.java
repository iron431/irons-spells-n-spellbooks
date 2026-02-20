package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record StatueData(UUID uuid, PlayerStatuePose pose, boolean flipped) {
    public static final Codec<StatueData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(StatueData::uuid),
            PlayerStatuePose.CODEC.fieldOf("pose").forGetter(StatueData::pose),
            Codec.BOOL.optionalFieldOf("flipped", false).forGetter(StatueData::flipped)
    ).apply(builder, StatueData::new));

    public StatueData updateUUID(UUID uuid) {
        return new StatueData(uuid, this.pose, this.flipped);
    }

    public StatueData updatePose(PlayerStatuePose pose) {
        return new StatueData(this.uuid, pose, this.flipped);
    }

    public StatueData updateFlipped(boolean flipped) {
        return new StatueData(this.uuid, this.pose, flipped);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode() * 31 + pose.getSerializedName().hashCode() + (flipped ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof StatueData data && data.uuid.equals(this.uuid) && data.pose.equals(this.pose) && data.flipped == this.flipped;
    }
}
