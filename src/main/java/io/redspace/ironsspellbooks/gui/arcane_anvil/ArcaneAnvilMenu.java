package io.redspace.ironsspellbooks.gui.arcane_anvil;

import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ArcaneAnvilMenu extends ItemCombinerMenu {
    public ArcaneAnvilMenu(int pContainerId, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuRegistry.ARCANE_ANVIL_MENU.get(), pContainerId, inventory, containerLevelAccess);
    }

    private final List<ItemStack> additionalDrops = new ArrayList<>();

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
            additionalDrops.forEach(stack -> {
                if (!stack.isEmpty()) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, stack));
                }
            });
            additionalDrops.clear();

        });
        createResult();
    }

    @Override
    protected boolean isValidBlock(BlockState pState) {
        return pState.is(BlockRegistry.ARCANE_ANVIL_BLOCK.get());
    }

    @Override
    public void createResult() {
        ItemStack result = ItemStack.EMPTY;
        this.additionalDrops.clear();
        ItemStack baseItemStack = inputSlots.getItem(0);
        ItemStack modifierItemStack = inputSlots.getItem(1);
        if (!baseItemStack.isEmpty() && !modifierItemStack.isEmpty()) {
            //
            // Scroll Upgrading
            if (ServerConfigs.SCROLL_MERGING.get() && baseItemStack.getItem() instanceof Scroll && modifierItemStack.getItem() instanceof InkItem inkItem) {
                SkillData spell1 = ISkillContainer.get(baseItemStack).getSkillAtIndex(0);
                if (spell1 != null && spell1.getSkill() instanceof AbstractSpell spellSkill) {
                    if (spell1.getLevel() < spellSkill.getMaxLevel()) {
                        SpellRarity nextRarity = spellSkill.getRarity(spell1.getLevel() + 1);
                        if (nextRarity.equals(inkItem.getRarity())) {
                            result = baseItemStack.copy();
                            result.setCount(1);
                            Scroll.applyScrollToStack(result, spellSkill, spell1.getLevel() + 1);
                        }
                    }
                }
            }
            //
            // Unique Item Improving
            else if (/*baseItemStack.getItem() instanceof MagicSwordItem && modifierItemStack.getItem() instanceof Scroll*/false) {
                // fixme: unique improvement detection
                SkillData scrollSlot = ISkillContainer.get(modifierItemStack).getSkillAtIndex(0);
                if (scrollSlot != null && ISkillContainer.isSkillContainer(baseItemStack)) {
                    ISkillContainer spellContainer = ISkillContainer.get(baseItemStack);
                    int matchIndex = spellContainer.getIndexForSkill(scrollSlot.getSkill());
                    if (matchIndex >= 0) {
                        SkillData spellData = spellContainer.getSkillAtIndex(matchIndex);
                        if (spellData != null && spellData.getLevel() < scrollSlot.getLevel() && spellData.isLocked()) {
                            result = baseItemStack.copy();
                            var newContainer = spellContainer.mutableCopy();
                            newContainer.removeSpellAtIndex(matchIndex);
                            newContainer.setSpellAtIndex(new SkillData(scrollSlot.getSkill(), scrollSlot.getLevel(), true), matchIndex);
                            ISkillContainer.set(result, newContainer.toImmutable());
                        }
                    }
                }
            }
            //
            // Generic Imbuement
            else if (Utils.canImbue(baseItemStack) && modifierItemStack.getItem() instanceof Scroll) {
                result = baseItemStack.copy();
                var spellContainer = Scroll.getOrCreateContainer(result, 1, true, false);

                SkillData scrollSlot = ISkillContainer.get(modifierItemStack).getSkillAtIndex(0);
                if (scrollSlot != null) {
                    int nextSlotIndex = spellContainer.getNextAvailableIndex();
                    if (nextSlotIndex == -1) {
                        nextSlotIndex = 0;
                    }
                    spellContainer.removeSpellAtIndex(nextSlotIndex);
                    spellContainer.setSpellAtIndex(new SkillData(scrollSlot.getSkill(), scrollSlot.getLevel(), false), nextSlotIndex);
                    ISkillContainer.set(result, spellContainer.toImmutable());
                }
            }
            //
            // Upgrade Orbs
            else if (Utils.canBeUpgraded(baseItemStack) && UpgradeData.getUpgradeData(baseItemStack).getTotalUpgrades() < ServerConfigs.MAX_UPGRADES.get() && modifierItemStack.has(ComponentRegistry.UPGRADE_ORB_TYPE)) {
                var upgradeKey = modifierItemStack.get(ComponentRegistry.UPGRADE_ORB_TYPE);
                var holderopt = this.player.registryAccess().holder(upgradeKey);
                if (holderopt.isPresent()) {
                    var upgradeOrb = holderopt.get();
                    result = baseItemStack.copy();
                    String slot = UpgradeUtils.getRelevantEquipmentSlot(result);
                    UpgradeData.getUpgradeData(result).addUpgrade(result, upgradeOrb, slot);
                }
            }
            //
            // Shriving Stone
            else if (modifierItemStack.is(ItemRegistry.SHRIVING_STONE.get())) {
                result = Utils.handleShriving(baseItemStack);
                UpgradeData upgradeData = UpgradeData.getUpgradeData(baseItemStack);
                upgradeData.upgrades().forEach((upgrade, count) -> upgrade.value().containerItem().map(stack -> {
                    stack.setCount(count);
                    return stack;
                }).ifPresent(additionalDrops::add));
            }
            //
            // Spell Slot Upgrade
            else if (modifierItemStack.getItem() instanceof SpellSlotUpgradeItem spellSlotUpgradeItem) {
                if (baseItemStack.getItem() instanceof SpellBook) {
                    ISkillContainer spellBookContainer = ISkillContainer.get(baseItemStack);
                    int max = spellSlotUpgradeItem.maxSlots();
                    if (spellBookContainer.getMaxSkillCount() < max) {
                        result = baseItemStack.copy();
                        var upgradedContainer = ISkillContainer.get(result).mutableCopy();
                        upgradedContainer.setMaxSpellCount(upgradedContainer.getMaxSkillCount() + 1);
                        ISkillContainer.set(result, upgradedContainer.toImmutable());
                    }
                }
            }
            //
            // Affinity Setting
            else if (baseItemStack.getItem() instanceof AffinityRing && modifierItemStack.getItem() instanceof Scroll) {
                result = baseItemStack.copy();
                SkillData scrollSlot = ISkillContainer.get(modifierItemStack).getSkillAtIndex(0);
                if (scrollSlot != null && scrollSlot.getSkill() instanceof AbstractSpell spellSkill) {
                    AffinityData.set(result, new AffinityData(spellSkill));
                }
            }
        }

        resultSlots.setItem(0, result);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
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
