package io.redspace.ironsspellbooks.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;

public class ExtendedSwordItem extends SwordItem {
    double attackDamage;
    double attackSpeed;
    private final SpellType imbuedSpell;
    private final int imbuedLevel;
    private final LazyOptional<Multimap<Attribute, AttributeModifier>> lazyOptional;
    LazyOptional<Map<Attribute, AttributeModifier>> additionalAttributes;

    public SpellType getImbuedSpell() {
        return imbuedSpell;
    }

    public int getImbuedLevel() {
        return imbuedLevel;
    }

    public ExtendedSwordItem(Tier tier, double attackDamage, double attackSpeed, SpellType imbuedSpell, int imbuedLevel, LazyOptional<Map<Attribute, AttributeModifier>> additionalAttributes, Properties properties) {
        super(tier, 3, -2.4f, properties);
        this.imbuedLevel = imbuedLevel;
        this.imbuedSpell = imbuedSpell;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.additionalAttributes = additionalAttributes;
        lazyOptional = LazyOptional.of(this::buildMap);
    }

    private Multimap<Attribute, AttributeModifier> buildMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        for (Map.Entry<Attribute, AttributeModifier> modifierEntry : additionalAttributes.resolve().get().entrySet()) {
            builder.put(modifierEntry.getKey(), modifierEntry.getValue());
        }
        return builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.lazyOptional.resolve().get() : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }
}
