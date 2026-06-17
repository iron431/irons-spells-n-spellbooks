package io.redspace.skillcasting.api.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class MultiTargetEntityCastComponent {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                try {
                    return DataResult.success(UUID.fromString(str));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Invalid UUID: " + str);
                }
            },
            UUID::toString);
    public static final Codec<MultiTargetEntityCastComponent> CODEC =
            Codec.list(UUID_CODEC).xmap(MultiTargetEntityCastComponent::new, MultiTargetEntityCastComponent::getTargets);

    private final List<UUID> targetUUIDs;

    public MultiTargetEntityCastComponent() {
        this(new ArrayList<>());
    }

    public MultiTargetEntityCastComponent(Entity... targets) {
        this(new ArrayList<>());
        Arrays.stream(targets).forEach(target -> targetUUIDs.add(target.getUUID()));
    }

    public MultiTargetEntityCastComponent(List<UUID> targetUUIDs) {
        this.targetUUIDs = new ArrayList<>(targetUUIDs);
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

    @Nullable
    public Entity getFirstEntityTarget(ServerLevel level) {
        for (UUID targetUUID : targetUUIDs) {
            Entity entity = level.getEntity(targetUUID);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    @Nullable
    public LivingEntity getFirstLivingEntityTarget(ServerLevel level) {
        var entity = getFirstEntityTarget(level);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MultiTargetEntityCastComponent other)) {
            return false;
        }
        return Objects.equals(targetUUIDs, other.targetUUIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(targetUUIDs);
    }
}
