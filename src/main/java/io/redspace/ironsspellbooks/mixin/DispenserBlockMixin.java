package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    @Unique
    @Nullable
    private static BlockState irons_spellbooks$blockStateCapture;
    @Unique
    @Nullable
    private static BlockPos irons_spellbooks$blockPosCapture;

    @Inject(method = "dispenseFrom", at = @At(value = "HEAD"), cancellable = true)
    private void irons_spellbooks$captureParameters(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci) {
        irons_spellbooks$blockStateCapture = state;
        irons_spellbooks$blockPosCapture = pos;
        DispenserBlockEntity dispenserblockentity = level.getBlockEntity(pos, BlockEntityType.DISPENSER).orElse(null);
        if (dispenserblockentity == null) {
            return;
        }
        if (SkillcastingData.has(dispenserblockentity) && SkillcastingData.get(dispenserblockentity).isCasting()) {
            SkillcastingManager.cancelCast(CasterRef.block(dispenserblockentity), CastEndReason.INTERRUPTED);
            ci.cancel();
            return;
        }
        // todo: this still allows any imbued item to cast freely, and completely shuts down the dispenser's ability to dispense non-skill items if any skill items are present
        //  fine for testing, not fine for gameplay
        Map<SkillSlot, ItemStack> skillOptions = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8).flatMap(i -> {
            ItemStack stack = dispenserblockentity.getItem(i);
            return ISkillContainer.isSkillContainer(stack)
                    ? ISkillContainer.get(stack).getActiveSkills().stream().map(slot -> Map.entry(slot, stack))
                    : Stream.empty();
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        if (skillOptions.isEmpty()) {
            return;
        }
        Map.Entry<SkillSlot, ItemStack> selectedEntry = new ArrayList<>(skillOptions.entrySet())
                .get(level.getRandom().nextInt(skillOptions.size()));
        SkillSlot skillSlot = selectedEntry.getKey();
        ItemStack stack = selectedEntry.getValue();
        var caster = CasterRef.block(dispenserblockentity);
        var context = SkillcastingManager.buildCastContext(caster, SkillRegistry.holder(skillSlot.getSkill()), skillSlot.getLevel(), CastSource.of("dispenser", ""), false);
        if (stack.is(ItemRegistry.SCROLL)) {
            stack.shrink(1);
        }
        SkillcastingManager.initiateCast(caster, context);
        ci.cancel();
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
