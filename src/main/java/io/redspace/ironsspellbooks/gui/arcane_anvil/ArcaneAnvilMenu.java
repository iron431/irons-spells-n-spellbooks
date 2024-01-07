package io.redspace.ironsspellbooks.gui.arcane_anvil;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
            if (baseItemStack.getItem() instanceof Scroll scroll1 && modifierItemStack.getItem() instanceof Scroll scroll2) {
                var spell1 = scroll1.initializeSpellContainer(baseItemStack).getSpellAtIndex(0);
                var spell2 = scroll2.initializeSpellContainer(modifierItemStack).getSpellAtIndex(0);

                if (spell1.equals(spell2)) {
                    if (spell1.getLevel() < ServerConfigs.getSpellConfig(spell1.getSpell()).maxLevel()) {
                        result = new ItemStack(ItemRegistry.SCROLL.get());
                        ISpellContainer.createScrollContainer(spell1.getSpell(), spell1.getLevel() + 1, result);
                    }
                }
            }
            //Unique Weapon Improving
            else if (baseItemStack.getItem() instanceof UniqueItem && modifierItemStack.getItem() instanceof Scroll scroll) {
                var scrollSlot = scroll.initializeSpellContainer(modifierItemStack).getSpellAtIndex(0);
                if (ISpellContainer.isSpellContainer(baseItemStack)) {
                    var spellContainer = ISpellContainer.get(baseItemStack);
                    var matchIndex = spellContainer.getIndexForSpell(scrollSlot.getSpell());
                    if (matchIndex >= 0) {
                        var spellData = spellContainer.getSpellAtIndex(matchIndex);
                        if (spellData.getLevel() < scrollSlot.getLevel() && spellData.isLocked()) {
                            result = baseItemStack.copy();
                            spellContainer.removeSpellAtIndex(matchIndex, null);
                            spellContainer.addSpellAtIndex(scrollSlot.getSpell(), scrollSlot.getLevel(), matchIndex, true, null);
                            SpellContainer.setNbtOnStack(result, spellContainer);
                            result.getOrCreateTag().putBoolean("Improved", true);
                        }
                    }
                }
            }
            //Weapon Imbuement
            else if (Utils.canImbue(baseItemStack) && modifierItemStack.getItem() instanceof Scroll scroll) {
                result = baseItemStack.copy();
                ISpellContainer spellContainer = ISpellContainer.getOrCreate(baseItemStack);

                var scrollSlot = scroll.initializeSpellContainer(modifierItemStack).getSpellAtIndex(0);
                var nextSlotIndex = spellContainer.getNextAvailableIndex();

                if (nextSlotIndex == -1) {
                    nextSlotIndex = 0;
                }

                spellContainer.removeSpellAtIndex(0, null);
                spellContainer.addSpellAtIndex(scrollSlot.getSpell(), scrollSlot.getLevel(), nextSlotIndex, false, null);
                spellContainer.save(result);
            }
            //Upgrade System
            else if (Utils.canBeUpgraded(baseItemStack) && UpgradeData.getUpgradeData(baseItemStack).getCount() < ServerConfigs.MAX_UPGRADES.get() && modifierItemStack.getItem() instanceof UpgradeOrbItem upgradeOrb) {
                result = baseItemStack.copy();
                String slot = UpgradeUtils.getRelevantEquipmentSlot(result);
                UpgradeData.getUpgradeData(result).addUpgrade(result, upgradeOrb.getUpgradeType(), slot);
                //IronsSpellbooks.LOGGER.debug("ArcaneAnvilMenu: upgrade system test: total upgrades on {}: {}", result.getDisplayName().getString(), UpgradeUtils.getUpgradeCount(result));
            }
            //Shriving Stone
            else if (modifierItemStack.is(ItemRegistry.SHRIVING_STONE.get())) {
                if (!(baseItemStack.getItem() instanceof Scroll)) {
                    if (ISpellContainer.isSpellContainer(baseItemStack)) {
                        result = baseItemStack.copy();
                        result.removeTagKey(SpellContainer.SPELL_SLOT_CONTAINER);

                    } else if (UpgradeData.hasUpgradeData(baseItemStack)) {
                        result = baseItemStack.copy();
                        result.removeTagKey(UpgradeData.Upgrades);

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
