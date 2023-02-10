package com.example.testmod.gui.arcane_anvil;

import com.example.testmod.config.ServerConfigs;
import com.example.testmod.item.Scroll;
import com.example.testmod.registries.BlockRegistry;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.registries.MenuRegistry;
import com.example.testmod.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneAnvilMenu extends ItemCombinerMenu {
    public ArcaneAnvilMenu(int pContainerId, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuRegistry.ARCANE_ANVIL_MENU.get(), pContainerId, inventory, containerLevelAccess);
    }


    public ArcaneAnvilMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(pContainerId, inventory, ContainerLevelAccess.NULL);
    }

    @Override
    protected boolean mayPickup(Player pPlayer, boolean pHasStack) {
        return true;
    }

    @Override
    protected void onTake(Player p_150601_, ItemStack p_150602_) {
        inputSlots.getItem(0).shrink(1);
        inputSlots.getItem(1).shrink(1);
    }

    @Override
    protected boolean isValidBlock(BlockState pState) {
        return pState.is(BlockRegistry.ARCANE_ANVIL_BLOCK.get());
    }

    @Override
    public void createResult() {
        ItemStack result = ItemStack.EMPTY;

        ItemStack baseItemStack = inputSlots.getItem(0);
        ItemStack modifierItemStack = inputSlots.getItem(1);
        if (!baseItemStack.isEmpty() && !modifierItemStack.isEmpty()) {
            //Scroll Merging
            if (baseItemStack.getItem() instanceof Scroll && modifierItemStack.getItem() instanceof Scroll) {
                var scrollData1 = Utils.getScrollData(baseItemStack);
                var scrollData2 = Utils.getScrollData(modifierItemStack);
                if (scrollData1.getSpellId() == scrollData2.getSpellId() && scrollData1.getLevel() == scrollData2.getLevel()) {
                    if (scrollData1.getLevel() < ServerConfigs.getSpellConfig(scrollData1.getSpellId()).MAX_LEVEL) {
                        result = new ItemStack(ItemRegistry.SCROLL.get());
                        var resultData = Utils.getScrollData(result);
                        resultData.setData(scrollData1.getSpellId(), scrollData1.getLevel() + 1);
                    }
                }
            }
            //Weapon Imbuement
            if (baseItemStack.getItem() instanceof SwordItem && modifierItemStack.getItem() instanceof Scroll) {
                result = baseItemStack.copy();
                var resultData = Utils.getScrollData(result);
                var scrollData = Utils.getScrollData(modifierItemStack);
                resultData.setData(scrollData.getSpell());

            }
        }

        resultSlots.setItem(0, result);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }
}
