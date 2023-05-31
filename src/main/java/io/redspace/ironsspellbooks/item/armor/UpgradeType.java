package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Optional;

public enum UpgradeType {
    FIRE_SPELL_POWER("fire_power", AttributeRegistry.FIRE_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ICE_SPELL_POWER("ice_power", AttributeRegistry.ICE_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    LIGHTNING_SPELL_POWER("lightning_power", AttributeRegistry.LIGHTNING_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    HOLY_SPELL_POWER("holy_power", AttributeRegistry.HOLY_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ENDER_SPELL_POWER("ender_power", AttributeRegistry.ENDER_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    BLOOD_SPELL_POWER("blood_power", AttributeRegistry.BLOOD_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    EVOCATION_SPELL_POWER("evocation_power", AttributeRegistry.EVOCATION_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    POISON_SPELL_POWER("poison_power", AttributeRegistry.POISON_SPELL_POWER.get(), AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    COOLDOWN("cooldown", AttributeRegistry.COOLDOWN_REDUCTION.get(), AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    SPELL_RESISTANCE("spell_resistance", AttributeRegistry.SPELL_RESIST.get(), AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    MANA("mana", AttributeRegistry.MAX_MANA.get(), AttributeModifier.Operation.ADDITION, 50),
    ATTACK_DAMAGE("melee_damage", Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.MULTIPLY_BASE, .03f),
    ATTACK_SPEED("melee_speed", Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_BASE, .06f),
    HEALTH("health", Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, 2),
    ;

    public final Attribute attribute;
    public final AttributeModifier.Operation operation;
    public final float amountPerUpgrade;
    public final String key;

    UpgradeType(String key, Attribute attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this.key = key;
        this.attribute = attribute;
        this.operation = operation;
        this.amountPerUpgrade = amountPerUpgrade;
    }

    public static Optional<UpgradeType> getUpgrade(String key) {
        for (UpgradeType upgradeType : UpgradeType.values())
            if (upgradeType.key.equals(key))
                return Optional.of(upgradeType);
        return Optional.empty();
    }
}
