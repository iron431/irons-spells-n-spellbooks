package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CinderousVaultBlock extends VaultBlock {
    public static final MapCodec<CinderousVaultBlock> CODEC = simpleCodec(CinderousVaultBlock::new);

    public CinderousVaultBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<VaultBlock> codec() {
        return (MapCodec<VaultBlock>) (Object) CODEC;
    }
}
