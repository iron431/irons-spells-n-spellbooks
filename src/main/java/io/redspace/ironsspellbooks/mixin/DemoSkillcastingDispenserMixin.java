package io.redspace.ironsspellbooks.mixin;

import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Stream;

@Mixin(DispenserBlock.class)
public class DemoSkillcastingDispenserMixin {
    @Inject(method = "dispenseFrom", at = @At("HEAD"), cancellable = true)
    private static void dispenseSpell(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci) {
        DispenserBlockEntity dispenserblockentity = level.getBlockEntity(pos, BlockEntityType.DISPENSER).orElse(null);
        if (dispenserblockentity == null) {
            return;
        }
        List<SkillSlot> skillOptions =
                Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8).flatMap(
                        i -> ISkillContainer.isSkillContainer(dispenserblockentity.getItem(i)) ? ISkillContainer.get(dispenserblockentity.getItem(i)).getActiveSkills().stream() : Stream.of()
                ).toList();
        if (skillOptions.isEmpty()) {
            return;
        }
        SkillSlot skillSlot = skillOptions.get(level.getRandom().nextInt(skillOptions.size()));
        var caster = CasterRef.block(dispenserblockentity);
        var context = SkillcastingManager.buildCastContext(caster, SkillRegistry.holder(skillSlot.getSkill()), skillSlot.getLevel(), null, false);
        SkillcastingManager.initiateCast(caster, context);
        ci.cancel();
    }
}
