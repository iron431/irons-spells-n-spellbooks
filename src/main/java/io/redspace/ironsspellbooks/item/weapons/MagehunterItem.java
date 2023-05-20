package io.redspace.ironsspellbooks.item.weapons;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;
import java.util.UUID;

public class MagehunterItem extends ExtendedSwordItem {
    public MagehunterItem(SpellType imbuedSpell, int imbuedLevel) {
        super(Tiers.DIAMOND, 6, -2.4f, imbuedSpell, imbuedLevel,
                LazyOptional.of(() -> Map.of(
                        AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(UUID.fromString("412b5a66-2b43-4c18-ab05-6de0bb4d64d3"), "Weapon Modifier", .15, AttributeModifier.Operation.MULTIPLY_BASE)
                )),
                (new Item.Properties()).tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).rarity(Rarity.EPIC));

    }
}
