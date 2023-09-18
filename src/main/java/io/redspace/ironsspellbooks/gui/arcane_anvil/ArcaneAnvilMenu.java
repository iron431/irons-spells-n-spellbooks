package io.redspace.ironsspellbooks.gui.arcane_anvil;

import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

        this.access.execute((level, pos) -> {
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, .8f, 1.1f);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1f, 1f);
        });
    }

    @Override
    protected boolean isValidBlock(BlockState pState) {
        return pState.is(BlockRegistry.ARCANE_ANVIL_BLOCK.get());
    }

    @Override
    public void createResult() {
        ItemStack result = ItemStack.EMPTY;
        /*
        Actions that can be taken in arcane anvil:
        - Upgrade scroll (scroll + scroll)
        - Imbue Weapon (weapon + scroll)
        - Upgrade item (item + upgrade orb)
         */
        ItemStack baseItemStack = inputSlots.getItem(0);
        ItemStack modifierItemStack = inputSlots.getItem(1);
        if (!baseItemStack.isEmpty() && !modifierItemStack.isEmpty()) {
            //Scroll Merging
            if (baseItemStack.getItem() instanceof Scroll && modifierItemStack.getItem() instanceof Scroll) {
                var spellData1 = SpellData.getSpellData(baseItemStack);
                var spellData2 = SpellData.getSpellData(modifierItemStack);
                if (spellData1.equals(spellData2)) {
                    if (spellData1.getLevel() < ServerConfigs.getSpellConfig(spellData1.getSpell()).maxLevel()) {
                        result = new ItemStack(ItemRegistry.SCROLL.get());
                        SpellData.setSpellData(result, spellData1.getSpell(), spellData1.getLevel() + 1);
                    }
                }

            }
            //Weapon Imbuement
            else if (Utils.canImbue(baseItemStack) && modifierItemStack.getItem() instanceof Scroll) {
                result = baseItemStack.copy();
                var scrollData = SpellData.getSpellData(modifierItemStack);
                SpellData.setSpellData(result, scrollData);
            }
            //Upgrade System
            else if (Utils.canBeUpgraded(baseItemStack) && UpgradeData.getUpgradeData(baseItemStack).getCount() < ServerConfigs.MAX_UPGRADES.get() && modifierItemStack.getItem() instanceof UpgradeOrbItem upgradeOrb) {
                result = baseItemStack.copy();
                EquipmentSlot slot = UpgradeUtils.getRelevantEquipmentSlot(result);
                UpgradeData.getUpgradeData(result).addUpgrade(result, upgradeOrb.getUpgradeType(), slot) ;
                //IronsSpellbooks.LOGGER.debug("ArcaneAnvilMenu: upgrade system test: total upgrades on {}: {}", result.getDisplayName().getString(), UpgradeUtils.getUpgradeCount(result));
            }
            //Shriving Stone
            else if (modifierItemStack.is(ItemRegistry.SHRIVING_STONE.get())) {
                if (!(baseItemStack.getItem() instanceof Scroll)) {
                    if (SpellData.hasSpellData(baseItemStack)) {
                        result = baseItemStack.copy();
                        result.removeTagKey(SpellData.ISB_SPELL);

                    } else if (UpgradeUtils.isUpgraded(baseItemStack)) {
                        result = baseItemStack.copy();
                        result.removeTagKey(UpgradeUtils.Upgrades);

                    }
                }
            }
        }

        resultSlots.setItem(0, result);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        //copied from anvil for 1.19.4
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 27, 47, (p_266635_) -> {
            return true;
        }).withSlot(1, 76, 47, (p_266634_) -> {
            return true;
        }).withResultSlot(2, 134, 47).build();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }
}
