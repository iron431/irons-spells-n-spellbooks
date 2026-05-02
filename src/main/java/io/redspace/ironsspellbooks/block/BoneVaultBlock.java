package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BoneVaultBlock extends VaultBlock {
    public static final MapCodec<BoneVaultBlock> CODEC = simpleCodec(BoneVaultBlock::new);

    public BoneVaultBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<VaultBlock> codec() {
        return (MapCodec<VaultBlock>) (Object) CODEC;
    }
}
