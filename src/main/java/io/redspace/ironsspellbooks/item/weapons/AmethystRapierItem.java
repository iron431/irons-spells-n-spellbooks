package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.world.item.Rarity;

import java.util.Map;

public class AmethystRapierItem extends MagicSwordItem {
    public AmethystRapierItem(SpellDataRegistryHolder[] imbuedSpells) {
        super(ExtendedWeaponTiers.AMETHYST, 7, -1.5f, imbuedSpells,
                Map.of(
//                        AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(UUID.fromString("412b5a66-2b43-4c18-ab05-6de0bb4d64d3"), "Weapon Modifier", .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                ),
                ItemPropertiesHelper.hidden(1).rarity(Rarity.EPIC));
    }
}
