package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public enum UpgradeTypes implements UpgradeType {
    FIRE_SPELL_POWER("fire_power", AttributeRegistry.FIRE_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ICE_SPELL_POWER("ice_power", AttributeRegistry.ICE_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    LIGHTNING_SPELL_POWER("lightning_power", AttributeRegistry.LIGHTNING_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    HOLY_SPELL_POWER("holy_power", AttributeRegistry.HOLY_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ENDER_SPELL_POWER("ender_power", AttributeRegistry.ENDER_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    BLOOD_SPELL_POWER("blood_power", AttributeRegistry.BLOOD_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    EVOCATION_SPELL_POWER("evocation_power", AttributeRegistry.EVOCATION_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    NATURE_SPELL_POWER("nature_power", AttributeRegistry.NATURE_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    COOLDOWN("cooldown", AttributeRegistry.COOLDOWN_REDUCTION.get(), AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    SPELL_RESISTANCE("spell_resistance", AttributeRegistry.SPELL_RESIST.get(), AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    MANA("mana", AttributeRegistry.MAX_MANA.get(), AttributeModifier.Operation.ADDITION, 50),
    ATTACK_DAMAGE("melee_damage", Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ATTACK_SPEED("melee_speed", Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    HEALTH("health", Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, 2),
    ;

    final Attribute attribute;
    final AttributeModifier.Operation operation;
    final float amountPerUpgrade;
    final ResourceLocation id;

    UpgradeTypes(String key, Attribute attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this.id = IronsSpellbooks.id(key);
        this.attribute = attribute;
        this.operation = operation;
        this.amountPerUpgrade = amountPerUpgrade;
        UpgradeType.registerUpgrade(this);
    }

    @Override
    public Attribute getAttribute() {
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
