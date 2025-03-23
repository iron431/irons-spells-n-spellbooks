package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
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

    @Inject(method = "dispenseFrom", at = @At(value = "HEAD"))
    private void irons_spellbooks$captureParameters(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci) {
        irons_spellbooks$blockStateCapture = state;
        irons_spellbooks$blockPosCapture = pos;
    }

    @Inject(method = "getDispenseMethod", at = @At(value = "HEAD"), cancellable = true)
    private void irons_spellbooks$injectCauldronInteractions(Level level, ItemStack item, CallbackInfoReturnable<DispenseItemBehavior> cir) {
        if (irons_spellbooks$blockStateCapture != null && irons_spellbooks$blockPosCapture != null &&
                level.getBlockEntity(irons_spellbooks$blockPosCapture.mutable().relative(irons_spellbooks$blockStateCapture.getValue(DirectionalBlock.FACING))) instanceof AlchemistCauldronTile alchemistCauldronTile) {
            ItemStack cauldronResult = alchemistCauldronTile.tryExecuteRecipeInteractions(level, item);
            if (!cauldronResult.isEmpty()) {
                cir.setReturnValue(new DefaultDispenseItemBehavior() {
                    @Override
                    protected ItemStack execute(BlockSource blockSource, ItemStack dispensingStack) {
                        return this.consumeWithRemainder(blockSource, dispensingStack, cauldronResult);
                    }
                });
            }
        }
    }
}
