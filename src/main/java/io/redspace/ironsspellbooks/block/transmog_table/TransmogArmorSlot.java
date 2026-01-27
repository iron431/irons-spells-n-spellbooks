package io.redspace.ironsspellbooks.block.transmog_table;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;
import java.util.Map;

class TransmogArmorSlot extends Slot {
    private final LivingEntity owner;
    private final EquipmentSlot slot;
    private final ResourceLocation emptyIcon;
    private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(
            EquipmentSlot.FEET,
            IronsSpellbooks.id("transmog_table/empty_armor_slot_boots"),
            EquipmentSlot.LEGS,
            IronsSpellbooks.id("transmog_table/empty_armor_slot_leggings"),
            EquipmentSlot.CHEST,
            IronsSpellbooks.id("transmog_table/empty_armor_slot_chestplate"),
            EquipmentSlot.HEAD,
            IronsSpellbooks.id("transmog_table/empty_armor_slot_helmet")
    );

    public TransmogArmorSlot(
            Container container, LivingEntity owner, EquipmentSlot slot, int slotIndex, int x, int y
    ) {
        super(container, slotIndex, x, y);
        this.owner = owner;
        this.slot = slot;
        this.emptyIcon = TEXTURE_EMPTY_SLOTS.get(slot);
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
        return (itemstack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
    }

    public ResourceLocation getEmptyIcon() {
        return emptyIcon;
    }
}