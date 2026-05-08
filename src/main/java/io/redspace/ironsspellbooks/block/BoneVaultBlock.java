package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public class BoneVaultBlock extends VaultBlock {
    public static final MapCodec<BoneVaultBlock> CODEC = simpleCodec(BoneVaultBlock::new);

    public BoneVaultBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull MapCodec<VaultBlock> codec() {
        return (MapCodec<VaultBlock>) (Object) CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        var entity = new VaultBlockEntity(blockPos, blockState);
        var config = new VaultConfig(
                ResourceKey.create(Registries.LOOT_TABLE, IronsSpellbooks.id("chests/catacombs/dead_king_vault")),
                4.0,
                4.5,
                new ItemStack(ItemRegistry.BONE_KEY),
                Optional.empty(),
                PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
        );
        entity.setConfig(config);
        return entity;
    }
}
