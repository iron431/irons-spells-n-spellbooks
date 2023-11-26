package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.render.SpecialItemRenderer;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;


public class TruthseekerItem extends ExtendedSwordItem {
    public TruthseekerItem() {
        super(ExtendedWeaponTiers.TRUTHSEEKER, 11, -3, Map.of(), new Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new SpecialItemRenderer(Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getEntityModels(),
                        "truthseeker");
            }
        });
    }
}
