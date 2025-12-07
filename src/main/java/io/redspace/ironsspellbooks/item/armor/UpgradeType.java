package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * I dont even remember how this worked, just use the new datadriven approach (See {@link UpgradeOrbTypeRegistry} for resource storage/datagen reference)
 */
@Deprecated(forRemoval = true)
public interface UpgradeType {

    Map<ResourceLocation, UpgradeType> UPGRADE_REGISTRY = new HashMap<>();

    static void registerUpgrade(UpgradeType upgrade) {
        UPGRADE_REGISTRY.put(upgrade.getId(), upgrade);
    }

    static Optional<UpgradeType> getUpgrade(ResourceLocation key) {
        UpgradeType upgradeType = UPGRADE_REGISTRY.get(key);
        return upgradeType == null ? Optional.empty() : Optional.of(upgradeType);
    }

    Holder<Attribute> getAttribute();

    AttributeModifier.Operation getOperation();

    float getAmountPerUpgrade();

    ResourceLocation getId();

    Optional<Holder<Item>> getContainerItem();
}
