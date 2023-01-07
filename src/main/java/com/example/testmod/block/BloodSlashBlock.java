package com.example.testmod.block;

import com.example.testmod.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;

public class BloodSlashBlock extends Block {

    public BloodSlashBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion());
    }

    public BloodSlashBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Entity entity) {
        if (entity instanceof Player player) {
            int duration = 200;
            int amplifier = 2;
            player.addEffect(new MobEffectInstance(MobEffectRegistry.BLOOD_SLASHED.get(), duration, amplifier));
        }
    }
}