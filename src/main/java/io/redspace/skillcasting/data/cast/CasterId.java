package io.redspace.skillcasting.data.cast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
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

public sealed interface CasterId permits CasterId.Entity, CasterId.Block, CasterId.Virtual {

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

    record Virtual(ResourceKey<Level> dimension, UUID id) implements CasterId {
        @Override
        public @Nullable CasterRef resolve(Level level) {
            return null;
        }
    }

    StreamCodec<RegistryFriendlyByteBuf, CasterId> STREAM_CODEC = StreamCodec.of(
            (buf, id) -> {
                switch (id) {
                    case Entity e -> {
                        buf.writeByte(0);
                        buf.writeInt(e.entityId);
                    }
                    case Block b -> {
                        buf.writeByte(1);
                        buf.writeResourceKey(b.dimension);
                        buf.writeBlockPos(b.pos);
                    }
                    case Virtual v -> {
                        buf.writeByte(2);
                        buf.writeResourceKey(v.dimension);
                        UUIDUtil.STREAM_CODEC.encode(buf, v.id);
                    }
                }
            },
            buf -> switch (buf.readByte()) {
                case 0 -> new Entity(buf.readInt());
                case 1 -> new Block(buf.readResourceKey(Registries.DIMENSION), buf.readBlockPos());
                case 2 -> new Virtual(buf.readResourceKey(Registries.DIMENSION), UUIDUtil.STREAM_CODEC.decode(buf));
                default -> throw new IllegalStateException("Unknown caster id kind");
            });
}
