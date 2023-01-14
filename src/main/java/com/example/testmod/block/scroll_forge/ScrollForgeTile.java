package com.example.testmod.block.scroll_forge;

import com.example.testmod.gui.scroll_forge.ScrollForgeMenu;
import com.example.testmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import static com.example.testmod.registries.BlockRegistry.SCROLL_FORGE_TILE;

public class ScrollForgeTile extends BlockEntity implements MenuProvider {
    public ScrollForgeTile(BlockPos pPos, BlockState pBlockState) {
        super(SCROLL_FORGE_TILE.get(), pPos, pBlockState);
    }

    private AbstractContainerMenu menu;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    @Override
    public MutableComponent getDisplayName() {
        return Component.literal("Scroll Forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        menu = new ScrollForgeMenu(containerId, inventory, this);
        return menu;
    }
}

