package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public enum UpgradeTypes implements UpgradeType {
    FIRE_SPELL_POWER("fire_power", AttributeRegistry.FIRE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ICE_SPELL_POWER("ice_power", AttributeRegistry.ICE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    LIGHTNING_SPELL_POWER("lightning_power", AttributeRegistry.LIGHTNING_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    HOLY_SPELL_POWER("holy_power", AttributeRegistry.HOLY_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ENDER_SPELL_POWER("ender_power", AttributeRegistry.ENDER_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    BLOOD_SPELL_POWER("blood_power", AttributeRegistry.BLOOD_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    EVOCATION_SPELL_POWER("evocation_power", AttributeRegistry.EVOCATION_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    NATURE_SPELL_POWER("nature_power", AttributeRegistry.NATURE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    COOLDOWN("cooldown", AttributeRegistry.COOLDOWN_REDUCTION, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    SPELL_RESISTANCE("spell_resistance", AttributeRegistry.SPELL_RESIST, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    MANA("mana", AttributeRegistry.MAX_MANA, AttributeModifier.Operation.ADD_VALUE, 50),
    ATTACK_DAMAGE("melee_damage", Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ATTACK_SPEED("melee_speed", Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    HEALTH("health", Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, 2),
    ;

    final Holder<Attribute> attribute;
    final AttributeModifier.Operation operation;
    final float amountPerUpgrade;
    final ResourceLocation id;

    UpgradeTypes(String key, Holder<Attribute> attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this.id = IronsSpellbooks.id(key);
        this.attribute = attribute;
        this.operation = operation;
        this.amountPerUpgrade = amountPerUpgrade;
        UpgradeType.registerUpgrade(this);
    }

    @Override
    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    @Override
    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    @Override
    public float getAmountPerUpgrade() {
        return amountPerUpgrade;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
}
