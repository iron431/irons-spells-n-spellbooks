package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.block.BloodCauldronBlock;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(CauldronBlock.class)
public abstract class CauldronMixin extends AbstractCauldronBlock {
    private int timer;

    public CauldronMixin(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pInteractions);
    }

    //@Inject(method = "tick", at = @At(value = "HEAD"))
    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
        if (++timer >= 20) {
            timer = 0;
            BloodCauldronBlock.cookEntity(blockState, level, pos, entity, () -> {
                level.setBlockAndUpdate(pos, BlockRegistry.BLOOD_CAULDRON_BLOCK.get().defaultBlockState());
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            });
        }
        super.entityInside(blockState, level, pos, entity);
    }
}
