package com.example.testmod.mixin;

import com.example.testmod.TestMod;
import com.example.testmod.block.BloodCauldronBlock;
import com.example.testmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
