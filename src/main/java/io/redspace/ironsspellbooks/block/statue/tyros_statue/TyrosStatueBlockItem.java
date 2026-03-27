package io.redspace.ironsspellbooks.block.statue.tyros_statue;

import io.redspace.ironspatreonlib.game.block.statue.decorative.client.DecorativeStatueItemClientExtensions;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class TyrosStatueBlockItem extends BlockItem {
    public TyrosStatueBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new DecorativeStatueItemClientExtensions(BlockRegistry.TYROS_STATUE_BLOCK::get, BlockRegistry.TYROS_STATUE_BLOCK_ENTITY));
    }
}
