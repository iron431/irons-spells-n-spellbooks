package io.redspace.ironsspellbooks.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import java.util.Map;

public class ExtendedSwordItem extends SwordItem implements UniqueItem {
    float attackDamage;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    private final SpellType imbuedSpell;
    private final int imbuedLevel;

    public ExtendedSwordItem(Tier tier, double attackDamage, double attackSpeed, SpellType imbuedSpell, int imbuedLevel, Map<Attribute, AttributeModifier> additionalAttributes, Properties properties) {
        super(tier, 3, -2.4f, properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (Map.Entry<Attribute, AttributeModifier> modifierEntry : additionalAttributes.entrySet()) {
            builder.put(modifierEntry.getKey(), modifierEntry.getValue());
        }
        this.defaultModifiers = builder.build();

        this.imbuedLevel = imbuedLevel;
        this.imbuedSpell = imbuedSpell;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }

    @Override
    public AbstractSpell[] getSpells() {
        return new AbstractSpell[]{AbstractSpell.getSpell(imbuedSpell, imbuedLevel)};
    }

//    @Nullable
//    @Override
//    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
//        var scrollDataProvider = new ScrollDataProvider();
//
//        if (nbt != null) {
//            //irons_spellbooks.LOGGER.debug("SwordItemMixin.initCapabilities.1: {}", nbt);
//            scrollDataProvider.deserializeNBT(nbt.getCompound(Scroll.PARENT));
//        } else {
//            //irons_spellbooks.LOGGER.debug("SwordItemMixin.initCapabilities.2");
//            scrollDataProvider.getOrCreateScrollData(this.imbuedSpell, this.imbuedLevel);
//        }
//        return scrollDataProvider;
//    }
}
