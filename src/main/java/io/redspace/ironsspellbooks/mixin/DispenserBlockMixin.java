package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    @Unique
    @Nullable
    private static BlockState irons_spellbooks$blockStateCapture;
    @Unique
    @Nullable
    private static BlockPos irons_spellbooks$blockPosCapture;
    @Unique
    @Nullable
    private static ServerLevel irons_spellbooks$levelCapture;

    @Inject(method = "dispenseFrom", at = @At(value = "HEAD"))
    private void irons_spellbooks$captureParameters(ServerLevel pLevel, BlockPos pPos, CallbackInfo ci) {
        irons_spellbooks$blockStateCapture = pLevel.getBlockState(pPos);
        irons_spellbooks$blockPosCapture = pPos;
        irons_spellbooks$levelCapture = pLevel;
    }

    @Inject(method = "getDispenseMethod", at = @At(value = "HEAD"), cancellable = true)
    private void irons_spellbooks$injectCauldronInteractions(ItemStack pStack, CallbackInfoReturnable<DispenseItemBehavior> cir) {
        if (irons_spellbooks$blockStateCapture != null && irons_spellbooks$blockPosCapture != null && irons_spellbooks$levelCapture != null &&
                irons_spellbooks$levelCapture.getBlockEntity(irons_spellbooks$blockPosCapture.mutable().relative(irons_spellbooks$blockStateCapture.getValue(DirectionalBlock.FACING))) instanceof AlchemistCauldronTile alchemistCauldronTile) {
            ItemStack cauldronResult = alchemistCauldronTile.tryExecuteRecipeInteractions(irons_spellbooks$levelCapture, pStack);
            if (!cauldronResult.isEmpty()) {
                cir.setReturnValue(new DefaultDispenseItemBehavior() {
                    @Override
                    protected ItemStack execute(BlockSource blockSource, ItemStack dispensingStack) {
//                        return this.consumeWithRemainder(blockSource, dispensingStack, cauldronResult);
                        var stack = dispensingStack;
                        var remainder = cauldronResult;
                        //copy+paste of 1.21#consumeWithRemainer
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            return remainder;
                        } else {
//                            this.addToInventoryOrDispense(blockSource, remainder);
                            //copy+paste of 1.21#addToInventoryOrDispense
                            var itemstack = remainder;
                            /*ItemStack itemstack*/
                            int i = ((DispenserBlockEntity) blockSource.getEntity()).addItem(remainder);
                            if (i == -1) {
                                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                                spawnItem(blockSource.getLevel(), itemstack, 6, direction, DispenserBlock.getDispensePosition(blockSource));
//                                playDefaultSound(blockSource);
//                                playDefaultAnimation(blockSource, direction);
                            }
                            return stack;
                        }
                    }
                });
            }
        }
    }
}
