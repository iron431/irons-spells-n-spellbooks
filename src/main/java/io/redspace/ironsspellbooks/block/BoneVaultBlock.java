package io.redspace.ironsspellbooks.block;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.PlayerDetector;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.VaultBlock;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data.VaultBlockEntity;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data.VaultConfig;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public class BoneVaultBlock extends VaultBlock {
//    public static final MapCodec<BoneVaultBlock> CODEC = simpleCodec(BoneVaultBlock::new);

    public BoneVaultBlock() {
        super(/*properties*/);
    }

//    @SuppressWarnings("unchecked")
//    @Override
//    public @NotNull MapCodec<VaultBlock> codec() {
//        return (MapCodec<VaultBlock>) (Object) CODEC;
//    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        var entity = new VaultBlockEntity(blockPos, blockState);
        var config = new VaultConfig(
                IronsSpellbooks.id("chests/catacombs/dead_king_vault"),
                4.0,
                4.5,
                new ItemStack(ItemRegistry.BONE_KEY.get()),
                Optional.empty(),
                PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
        );
        entity.setConfig(config);
        return entity;
    }
}
