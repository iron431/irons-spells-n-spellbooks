package io.redspace.ironsspellbooks.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Map;
import java.util.UUID;

public class BloodStaffItem extends StaffItem {

    public BloodStaffItem(SpellDataRegistryHolder[] spellDataRegistryHolders) {
        super(ItemPropertiesHelper.equipment().stacksTo(1).rarity(Rarity.UNCOMMON), 7, -3, Map.of(AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier(UUID.fromString("667ad88f-901d-4691-b2a2-3664e42026d3"), "Weapon modifier", .10, AttributeModifier.Operation.MULTIPLY_BASE)));
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (pStack.hasTag() && pStack.getTag().getBoolean("Improved")) {
            return Component.translatable("item.irons_spellbooks.improved_blood_staff");
        } else {
            return Component.translatable(this.getDescriptionId(pStack));
        }
    }
}
