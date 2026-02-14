package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record StatueItemData(UUID uuid) {
    public static final Codec<StatueItemData> CODEC = UUIDUtil.CODEC.xmap(StatueItemData::new, StatueItemData::uuid);

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof StatueItemData data && data.uuid.equals(this.uuid);
    }
}
