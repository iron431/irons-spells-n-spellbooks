package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.render.SpecialItemRenderer;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


public class KeeperFlambergeItem extends ExtendedSwordItem {
    public KeeperFlambergeItem() {
        super(ExtendedWeaponTiers.KEEPER_FLAMBERGE, 10, -2.7, Map.of(Attributes.ARMOR, new AttributeModifier(UUID.fromString("c552273e-6669-4cd2-80b3-a703b7616336"), "weapon mod", 5, AttributeModifier.Operation.ADDITION)), ItemPropertiesHelper.equipment().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new SpecialItemRenderer(Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getEntityModels(),
                        "keeper_flamberge");
            }
        });
    }
}
