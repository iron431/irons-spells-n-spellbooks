package io.redspace.skillcasting.data.cast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public interface CasterId {

    @Nullable
    CasterRef resolve(Level level);

    static Entity entity(@NotNull net.minecraft.world.entity.Entity entity) {
        return new Entity(entity.getId());
    }

    static Block block(@NotNull BlockEntity blockEntity) {
        return new Block(Objects.requireNonNull(blockEntity.getLevel(), "Cannot ID a block entity outside of a level").dimension(), blockEntity.getBlockPos());
    }


    record Entity(int entityId) implements CasterId {
        @Override
        public @Nullable CasterRef resolve(Level level) {
            var entity = level.getEntity(this.entityId);
            return entity == null ? null : CasterRef.entity(entity);
        }
    }

    record Block(ResourceKey<Level> dimension, BlockPos pos) implements CasterId {
        @Override
        public @Nullable CasterRef resolve(Level level) {
            if (!level.dimension().equals(this.dimension)) {
                return null;
            }
            var blockEntity = level.getBlockEntity(this.pos);
            return blockEntity == null ? null : CasterRef.block(blockEntity);
        }
    }

    /**
     * Stub for a future non-entity, non-block caster identified by a UUID the consumer defines resolution for.
     * Not implemented nor resolvable, do not use
     */
    record Custom(UUID id) implements CasterId {
        @Override
        public @Nullable CasterRef resolve(Level level) {
            return null;
        }
    }

    static Custom custom(UUID id) {
        return new Custom(id);
    }

    StreamCodec<RegistryFriendlyByteBuf, CasterId> STREAM_CODEC = StreamCodec.of(
            (buf, id) -> {
                if (id instanceof Entity e) {
                    buf.writeBoolean(true);
                    buf.writeInt(e.entityId);
                } else if (id instanceof Block b) {
                    buf.writeBoolean(false);
                    buf.writeResourceKey(b.dimension);
                    buf.writeBlockPos(b.pos);
                } else {
                    throw new UnsupportedOperationException("CasterId.Custom has no wire format yet");
                }
            },
            buf -> {
                if (buf.readBoolean()) {
                    return new Entity(buf.readInt());
                } else {
                    return new Block(buf.readResourceKey(Registries.DIMENSION), buf.readBlockPos());
                }
            });
}
