package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

import java.util.Optional;

public enum UpgradeTypes implements UpgradeType {
    FIRE_SPELL_POWER("fire_power", ItemRegistry.FIRE_UPGRADE_ORB, AttributeRegistry.FIRE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ICE_SPELL_POWER("ice_power", ItemRegistry.ICE_UPGRADE_ORB, AttributeRegistry.ICE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    LIGHTNING_SPELL_POWER("lightning_power", ItemRegistry.LIGHTNING_UPGRADE_ORB, AttributeRegistry.LIGHTNING_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    HOLY_SPELL_POWER("holy_power", ItemRegistry.HOLY_UPGRADE_ORB, AttributeRegistry.HOLY_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ENDER_SPELL_POWER("ender_power", ItemRegistry.ENDER_UPGRADE_ORB, AttributeRegistry.ENDER_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    BLOOD_SPELL_POWER("blood_power", ItemRegistry.BLOOD_UPGRADE_ORB, AttributeRegistry.BLOOD_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    EVOCATION_SPELL_POWER("evocation_power", ItemRegistry.EVOCATION_UPGRADE_ORB, AttributeRegistry.EVOCATION_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    NATURE_SPELL_POWER("nature_power", ItemRegistry.NATURE_UPGRADE_ORB, AttributeRegistry.NATURE_SPELL_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    COOLDOWN("cooldown", ItemRegistry.COOLDOWN_UPGRADE_ORB, AttributeRegistry.COOLDOWN_REDUCTION, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    SPELL_RESISTANCE("spell_resistance", ItemRegistry.PROTECTION_UPGRADE_ORB, AttributeRegistry.SPELL_RESIST, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    MANA("mana", ItemRegistry.MANA_UPGRADE_ORB, AttributeRegistry.MAX_MANA, AttributeModifier.Operation.ADD_VALUE, 50),
    ATTACK_DAMAGE("melee_damage", Optional.empty(), Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    ATTACK_SPEED("melee_speed", Optional.empty(), Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, .05f),
    HEALTH("health", Optional.empty(), Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, 2),
    ;

    final Holder<Attribute> attribute;
    final AttributeModifier.Operation operation;
    final float amountPerUpgrade;
    final ResourceLocation id;
    final Optional<Holder<Item>> containerItem;

    UpgradeTypes(String key, Holder<Item> containerItem, Holder<Attribute> attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this(key, Optional.of(containerItem), attribute, operation, amountPerUpgrade);
    }

    UpgradeTypes(String key, Optional<Holder<Item>> containerItem, Holder<Attribute> attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this.id = IronsSpellbooks.id(key);
        this.attribute = attribute;
        this.operation = operation;
        this.amountPerUpgrade = amountPerUpgrade;
        this.containerItem = containerItem;
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

    @Override
    public Optional<Holder<Item>> getContainerItem() {
        return containerItem;
    }
}
