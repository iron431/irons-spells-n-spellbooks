package io.redspace.ironsspellbooks.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public class StaffItem extends CastingItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public StaffItem(Item.Properties properties, StaffTier staffTier) {
        super(properties);

        float attackDamage = staffTier.getAttackDamageBonus();
        float attackSpeed = staffTier.getSpeed();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (AttributeContainer container : staffTier.getAdditionalAttributes()) {
            builder.put(container.attribute().get(), container.createModifier(EquipmentSlot.MAINHAND.getName()));
        }
        this.defaultModifiers = builder.build();
    }

    /**
     * Use {@link StaffTier} and {@link StaffItem#StaffItem(Properties, StaffTier)}
     */
    @Deprecated(forRemoval = true)
    public StaffItem(Item.Properties properties, double attackDamage, double attackSpeed, Map<Attribute, AttributeModifier> additionalAttributes) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (Map.Entry<Attribute, AttributeModifier> modifierEntry : additionalAttributes.entrySet()) {
            builder.put(modifierEntry.getKey(), modifierEntry.getValue());
        }
        this.defaultModifiers = builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    public boolean hasCustomRendering(){
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        StaffArmPose.initializeClientHelper(consumer);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }
}
