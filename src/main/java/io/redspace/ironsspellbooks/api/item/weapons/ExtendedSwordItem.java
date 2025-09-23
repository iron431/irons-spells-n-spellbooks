package io.redspace.ironsspellbooks.api.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.IronsWeaponTier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExtendedSwordItem extends SwordItem {

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public <T extends Tier & IronsWeaponTier> ExtendedSwordItem(T tier, Properties pProperties) {
        super(tier, (int) tier.getAttackDamageBonus(), tier.getSpeed(), pProperties);

        float attackDamage = tier.getAttackDamageBonus();
        float attackSpeed = tier.getSpeed();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (AttributeContainer container : tier.getAdditionalAttributes()) {
            builder.put(container.attribute().get(), container.createModifier(EquipmentSlot.MAINHAND.getName()));
        }
        this.defaultModifiers = builder.build();
    }

    /**
     * Deprecated 1.20.1 API compat.  Use {@link IronsWeaponTier} and {@link ExtendedSwordItem#ExtendedSwordItem(Tier, Properties)} instead
     */
    @Deprecated(forRemoval = true)
    public ExtendedSwordItem(Tier tier, double attackDamage, double attackSpeed, Map<Attribute, AttributeModifier> additionalAttributes, Properties properties) {
        super(tier, 3, -2.4f, properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (Map.Entry<Attribute, AttributeModifier> modifierEntry : additionalAttributes.entrySet()) {
            builder.put(modifierEntry.getKey(), modifierEntry.getValue());
        }
        this.defaultModifiers = builder.build();
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }
//    public static ItemAttributeModifiers createAttributes(IronsWeaponTier pTier) {
//        var builder = ItemAttributeModifiers.builder()
//                .add(
//                        Attributes.ATTACK_DAMAGE,
//                        new AttributeModifier(
//                                BASE_ATTACK_DAMAGE_ID, pTier.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION
//                        ),
//                        EquipmentSlotGroup.MAINHAND
//                )
//                .add(
//                        Attributes.ATTACK_SPEED,
//                        new AttributeModifier(BASE_ATTACK_SPEED_ID, pTier.getSpeed(), AttributeModifier.Operation.ADDITION),
//                        EquipmentSlotGroup.MAINHAND
//                );
//        for (AttributeContainer holder : pTier.getAdditionalAttributes()) {
//            builder.add(holder.attribute(), holder.createModifier(EquipmentSlot.MAINHAND.getName()), EquipmentSlotGroup.MAINHAND);
//        }
//        return builder.build();
//    }
}
