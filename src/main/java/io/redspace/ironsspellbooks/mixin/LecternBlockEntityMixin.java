package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.ILecternPlaceable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public abstract class LecternBlockEntityMixin extends BlockEntity {
    public LecternBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Shadow
    ItemStack book;

    @Shadow
    private int page;


    @Inject(
            method = "hasBook",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private void fudgeLecternValidity(CallbackInfoReturnable<Boolean> cir) {
        if (book.getItem() instanceof ILecternPlaceable) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "getPageCount",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void getPageCount(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        if (pStack.getItem() instanceof ILecternPlaceable lecternPlaceable) {
            cir.setReturnValue(lecternPlaceable.getPages(pStack).size());
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }
}
