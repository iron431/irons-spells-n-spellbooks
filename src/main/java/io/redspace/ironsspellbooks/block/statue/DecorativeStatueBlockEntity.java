package io.redspace.ironsspellbooks.block.statue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class DecorativeStatueBlockEntity extends BlockEntity {

    public static <T extends DecorativeStatueBlockEntity> BlockEntityType.BlockEntitySupplier<T> from(Supplier<BlockEntityType<T>> type) {
        return (pos, state) -> (T) new DecorativeStatueBlockEntity(type.get(), pos, state);
    }

    public DecorativeStatueBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }


    //fixme: this is duplicated from PlayerStatueBlockEntity. Make abstract parent class?
    /*----------------------------------
     * Multiblock Handling
     *----------------------------------*/
    public boolean isPrimary() {
        var state = this.getBlockState();
        return state.getValue(AbstractStatueBlock.X_POS) == 0 && state.getValue(AbstractStatueBlock.Y_POS) == 0 && state.getValue(AbstractStatueBlock.Z_POS) == 0;
    }

    public @NotNull Optional<DecorativeStatueBlockEntity> getPrimaryControllerOpt() {
        return Optional.ofNullable(getPrimaryController());
    }

    @Nullable
    public DecorativeStatueBlockEntity getPrimaryController() {
        var state = this.getBlockState();
        int xPos = state.getValue(AbstractStatueBlock.X_POS);
        int yPos = state.getValue(AbstractStatueBlock.Y_POS);
        int zPos = state.getValue(AbstractStatueBlock.Z_POS);
        if (xPos == 0 && yPos == 0 && zPos == 0) {
            return this;
        } else if (level != null && level.getBlockEntity(this.getBlockPos().offset(-xPos, -yPos, -zPos)) instanceof DecorativeStatueBlockEntity statueBlock) {
            return statueBlock;
        }
        return null;
    }
}
