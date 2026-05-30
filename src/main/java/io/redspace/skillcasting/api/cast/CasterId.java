package io.redspace.skillcasting.api.cast;

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

//todo: may reintroduce a type field to expand into a "Custom" type, which is manually sync or something, with a UUID payload the end user can figure out how to match on their own.
// maybe even java's object-to-json shenanigans?
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
//        public static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Level>> DIMENSION_KEY_CODEC =
//                StreamCodec.of(FriendlyByteBuf::writeResourceKey, buf -> buf.readResourceKey(Registries.DIMENSION));
//
//        public static final StreamCodec<RegistryFriendlyByteBuf, Block> STREAM_CODEC =
//                StreamCodec.composite(
//                        DIMENSION_KEY_CODEC, Block::dimension,
//                        BlockPos.STREAM_CODEC, Block::pos,
//                        Block::new);

        @Override
        public @Nullable CasterRef resolve(Level level) {
            if (!level.dimension().equals(this.dimension)) {
                return null;
            }
            var blockEntity = level.getBlockEntity(this.pos);
            return blockEntity == null ? null : CasterRef.block(blockEntity);
        }
    }

    StreamCodec<RegistryFriendlyByteBuf, CasterId> STREAM_CODEC = StreamCodec.of(
            (buf, id) -> {
                if (id instanceof Entity e) {
                    buf.writeBoolean(true);
                    buf.writeInt(e.entityId);
                } else {
                    var b = (Block) id;
                    buf.writeBoolean(false);
                    buf.writeResourceKey(b.dimension);
                    buf.writeBlockPos(b.pos);
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
