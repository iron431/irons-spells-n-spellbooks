package io.redspace.ironsspellbooks.item.curios;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class CurioBaseItem extends Item implements ICurioItem {
    String attributeSlot = "";
    Multimap<Holder<Attribute>, AttributeModifier> attributes = null;

    public CurioBaseItem(Item.Properties properties) {
        super(properties);
    }

    public boolean isEquippedBy(@Nullable LivingEntity entity) {
        return entity != null && CuriosApi.getCuriosInventory(entity).map(inv -> inv.findFirstCurio(this).isPresent()).orElse(false);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 1.0f, 1.0f);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        if (!slotContext.identifier().equals(this.attributeSlot)) {
            return attributes;
        } else {
            // each AttributeModifier must have a unique ResourceLocation
            // we deduplicate by adding the slot identifier and index to the original ResourceLocation
            ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
            for (var entry : attributes.entries()) {
                AttributeModifier original = entry.getValue();
                builder.put(entry.getKey(), new AttributeModifier(
                        IronsSpellbooks.id(String.format("%s.%s_%d", original.id().getPath(), slotContext.identifier(), slotContext.index())),
                        original.amount(), original.operation()));
            }

            return builder.build();
        }
    }

    public CurioBaseItem withAttributes(String slot, AttributeContainer... attributes) {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        for (AttributeContainer holder : attributes) {
            builder.put(holder.attribute(), holder.createModifier(slot));
        }
        this.attributes = builder.build();
        this.attributeSlot = slot;
        return this;
    }

    public CurioBaseItem withSpellbookAttributes(AttributeContainer... attributes) {
        return withAttributes(Curios.SPELLBOOK_SLOT, attributes);
    }
}
