package io.redspace.ironsspellbooks.registries;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Optional;

public class UpgradeOrbTypeRegistry {
    public static final ResourceKey<Registry<UpgradeOrbType>> UPGRADE_ORB_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsSpellbooks.id("upgrade_orb_type"));
    public static final Codec<Holder<UpgradeOrbType>> UPGRADE_ORB_REGISTRY_CODEC = RegistryFixedCodec.create(UPGRADE_ORB_REGISTRY_KEY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<UpgradeOrbType>> UPGRADE_ORB_REGISTRY_STREAM_CODEC = ByteBufCodecs.holderRegistry(UPGRADE_ORB_REGISTRY_KEY);

    public static Registry<UpgradeOrbType> upgradeTypeRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(UPGRADE_ORB_REGISTRY_KEY);
    }

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(UPGRADE_ORB_REGISTRY_KEY, UpgradeOrbType.CODEC, UpgradeOrbType.CODEC);
    }

    public static ResourceKey<UpgradeOrbType> FIRE_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("fire_power"));
    public static ResourceKey<UpgradeOrbType> ICE_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("ice_power"));
    public static ResourceKey<UpgradeOrbType> LIGHTNING_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("lightning_power"));
    public static ResourceKey<UpgradeOrbType> HOLY_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("holy_power"));
    public static ResourceKey<UpgradeOrbType> ENDER_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("ender_power"));
    public static ResourceKey<UpgradeOrbType> BLOOD_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("blood_power"));
    public static ResourceKey<UpgradeOrbType> EVOCATION_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("evocation_power"));
    public static ResourceKey<UpgradeOrbType> NATURE_SPELL_POWER = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("nature_power"));
    public static ResourceKey<UpgradeOrbType> COOLDOWN = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("cooldown"));
    public static ResourceKey<UpgradeOrbType> SPELL_RESISTANCE = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("spell_resistance"));
    public static ResourceKey<UpgradeOrbType> MANA = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("mana"));
    public static ResourceKey<UpgradeOrbType> ATTACK_DAMAGE = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("melee_damage"));
    public static ResourceKey<UpgradeOrbType> ATTACK_SPEED = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("melee_speed"));
    public static ResourceKey<UpgradeOrbType> HEALTH = ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("health"));

    public static void bootstrap(BootstrapContext<UpgradeOrbType> bootstrap) {
        bootstrap.register(FIRE_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.FIRE_SPELL_POWER, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.FIRE_UPGRADE_ORB));
        bootstrap.register(ICE_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.ICE_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.ICE_UPGRADE_ORB));
        bootstrap.register(LIGHTNING_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.LIGHTNING_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.LIGHTNING_UPGRADE_ORB));
        bootstrap.register(HOLY_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.HOLY_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.HOLY_UPGRADE_ORB));
        bootstrap.register(ENDER_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.ENDER_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.ENDER_UPGRADE_ORB));
        bootstrap.register(BLOOD_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.BLOOD_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.BLOOD_UPGRADE_ORB));
        bootstrap.register(EVOCATION_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.EVOCATION_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.EVOCATION_UPGRADE_ORB));
        bootstrap.register(NATURE_SPELL_POWER,
                new UpgradeOrbType(AttributeRegistry.NATURE_SPELL_POWER, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.NATURE_UPGRADE_ORB));
        bootstrap.register(COOLDOWN,
                new UpgradeOrbType(AttributeRegistry.COOLDOWN_REDUCTION, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.COOLDOWN_UPGRADE_ORB));
        bootstrap.register(SPELL_RESISTANCE,
                new UpgradeOrbType(AttributeRegistry.SPELL_RESIST, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.PROTECTION_UPGRADE_ORB));
        bootstrap.register(MANA,
                new UpgradeOrbType(AttributeRegistry.MAX_MANA, 50, AttributeModifier.Operation.ADD_VALUE, ItemRegistry.MANA_UPGRADE_ORB));
        bootstrap.register(ATTACK_DAMAGE,
                new UpgradeOrbType(Attributes.ATTACK_DAMAGE, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Optional.empty()));
        bootstrap.register(ATTACK_SPEED,
                new UpgradeOrbType(Attributes.ATTACK_SPEED, .05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Optional.empty()));
        bootstrap.register(HEALTH,
                new UpgradeOrbType(Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.ADD_VALUE,
                        Optional.empty()));
    }
}
