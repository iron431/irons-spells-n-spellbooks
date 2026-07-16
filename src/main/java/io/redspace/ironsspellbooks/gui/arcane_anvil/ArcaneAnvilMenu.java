package io.redspace.ironsspellbooks.gui.arcane_anvil;

import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import io.redspace.ironsspellbooks.item.spell_containers.ImbuedContainer;
import io.redspace.ironsspellbooks.item.spell_containers.ScrollContainer;
import io.redspace.ironsspellbooks.item.spell_containers.SpellbookContainer;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
                SkillData spell1 = ScrollContainer.getScrollData(baseItemStack);
                if (spell1 != null && spell1.getSkill() instanceof AbstractSpell spellSkill) {
                    if (spell1.getLevel() < spellSkill.getMaxLevel()) {
                        SpellRarity nextRarity = spellSkill.getRarity(spell1.getLevel() + 1);
                        if (nextRarity.equals(inkItem.getRarity())) {
                            result = baseItemStack.copy();
                            result.setCount(1);
                            ScrollContainer.set(result, spellSkill, spell1.getLevel() + 1);
                        }
                    }
                }
            }
            //
            // Scroll Modifiers
            else if (modifierItemStack.getItem() instanceof Scroll) {
                SkillData scrollSlot = ScrollContainer.getScrollData(modifierItemStack);
                if (scrollSlot != null) {
                    //
                    // Generic Imbuement
                    if (Utils.canImbue(baseItemStack)) {
                        var imbueContainer = ImbuedContainer.has(baseItemStack) ? ImbuedContainer.get(baseItemStack).mutableCopy() : ImbuedContainer.create(1, false).mutableCopy();
                        int nextSlotIndex = imbueContainer.getIndexForSkill(scrollSlot.getSkill());
                        if (nextSlotIndex == -1) {
                            nextSlotIndex = imbueContainer.getNextAvailableIndex();
                        }
                        if (nextSlotIndex == -1) {
                            nextSlotIndex = 0;
                        }
                        SkillData overridden = imbueContainer.getSkillAtIndex(nextSlotIndex);

                        if (overridden == null || !overridden.isLocked()) {
                            result = baseItemStack.copy();
                            imbueContainer.removeSpellAtIndex(nextSlotIndex);
                            imbueContainer.setSpellAtIndex(new SkillData(scrollSlot.getSkill(), scrollSlot.getLevel(), false), nextSlotIndex);
                            ImbuedContainer.set(result, imbueContainer.toImmutable());
                        }
                    }
                    //
                    // Imbued Locked Slot Improvement (Unique Improvement)
                    if (result.isEmpty() && ImbuedContainer.has(baseItemStack)) {
                        result = handleLockedImprovement(baseItemStack, scrollSlot, ComponentRegistry.IMBUED_SPELL_CONTAINER.get(), result);
                    }
                    //
                    // Spellbook Locked Slot Improvement (Unique Improvement)
                    if (result.isEmpty() && SpellbookContainer.has(baseItemStack)) {
                        result = handleLockedImprovement(baseItemStack, scrollSlot, ComponentRegistry.SPELLBOOK_CONTAINER.get(), result);
                    }
                    //
                    // Affinity Setting
                    if (baseItemStack.getItem() instanceof AffinityRing) {
                        result = baseItemStack.copy();
                        if (scrollSlot.getSkill() instanceof AbstractSpell spellSkill) {
                            AffinityData.set(result, new AffinityData(spellSkill));
                        }
                    }
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
                    ISkillContainer spellBookContainer = SpellbookContainer.get(baseItemStack);
                    int max = spellSlotUpgradeItem.maxSlots();
                    if (spellBookContainer.getMaxSkillCount() < max) {
                        result = baseItemStack.copy();
                        var upgradedContainer = SpellbookContainer.get(result).mutableCopy();
                        upgradedContainer.setMaxSpellCount(upgradedContainer.getMaxSkillCount() + 1);
                        SpellbookContainer.set(result, upgradedContainer.toImmutable());
                    }
                }
            }
        }

        resultSlots.setItem(0, result);
    }

    private static <T extends ISkillContainer> ItemStack handleLockedImprovement(ItemStack baseItemStack, SkillData scrollSlot, DataComponentType<T> container, ItemStack result) {
        var spellContainer = baseItemStack.get(container).mutableCopy();
        int nextSlotIndex = spellContainer.getIndexForSkill(scrollSlot.getSkill());
        if (nextSlotIndex != -1) {
            SkillData data = spellContainer.getSkillAtIndex(nextSlotIndex);
            if (data != null && data.isLocked() && data.getHolder().equals(scrollSlot.getHolder()) && data.getLevel() < scrollSlot.getLevel()) {
                result = baseItemStack.copy();
                spellContainer.removeSpellAtIndex(nextSlotIndex);
                spellContainer.setSpellAtIndex(new SkillData(scrollSlot.getSkill(), scrollSlot.getLevel(), false), nextSlotIndex);
                if (!result.has(DataComponents.ITEM_NAME)) {
                    result.set(DataComponents.ITEM_NAME, Component.translatable("tooltip.irons_spellbooks.improved_format", result.getHoverName()));
                }
                result.set(container, (T) spellContainer.toImmutable());
                return result;
            }
        }
        return result;
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
