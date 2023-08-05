package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.render.SpecialItemRenderer;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MagehunterItem extends ExtendedSwordItem {

    public MagehunterItem() {
        super(ExtendedWeaponTiers.METAL_MAGEHUNTER, 6, -2.4f,
                /*SpellType.COUNTERSPELL_SPELL, 1,*/
                Map.of(
                        AttributeRegistry.SPELL_RESIST.get(), new AttributeModifier(UUID.fromString("412b5a66-2b43-4c18-ab05-6de0bb4d64d3"), "Weapon Modifier", .25, AttributeModifier.Operation.MULTIPLY_BASE)
                ),
                (new Item.Properties()).tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new SpecialItemRenderer(Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getEntityModels(),
                        "magehunter");
            }
        });
    }
}
