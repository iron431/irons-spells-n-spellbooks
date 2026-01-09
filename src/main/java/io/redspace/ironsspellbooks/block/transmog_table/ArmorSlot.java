package io.redspace.ironsspellbooks.block.transmog_table;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;

//todo: accessor for vanilla's damnned armor slot
@Deprecated(forRemoval = true)
class ArmorSlot extends Slot {
    private final LivingEntity owner;
    private final EquipmentSlot slot;
    @Nullable
    private final ResourceLocation emptyIcon;

    public ArmorSlot(
            Container container, LivingEntity owner, EquipmentSlot slot, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon
    ) {
        super(container, slotIndex, x, y);
        this.owner = owner;
        this.slot = slot;
        this.emptyIcon = emptyIcon;
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        this.owner.onEquipItem(this.slot, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.canEquip(slot, owner);
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemstack = this.getItem();
        return !itemstack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
            ? false
            : super.mayPickup(player);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
    }
}