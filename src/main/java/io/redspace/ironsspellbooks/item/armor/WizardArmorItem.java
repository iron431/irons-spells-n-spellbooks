package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.ClothingVariantHelper;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import io.redspace.ironsspellbooks.entity.armor.DyeableArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.GenericArmorModel;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.Map;

public class WizardArmorItem extends ImbuableChestplateArmorItem implements IDisableJacket, DyeableLeatherItem, IBackwardsCompatDefaultNbtItem {
    private static final String descIdHat = "item.irons_spellbooks.wizard_helmet.hat";
    private static final String descIdHood = "item.irons_spellbooks.wizard_helmet.hood";

    public WizardArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.WIZARD, type, settings/*, withManaAndSpellPowerAttribute(125, 0.05)*/);
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ArmorItem armorItem) || armorItem.getType() != Type.HELMET) {
            return super.getDescriptionId(stack);
        } else {
            return ClothingVariantHelper.getClothingVariantOrElse(stack, "").equals("hat") ? descIdHat : descIdHood;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new DyeableArmorRenderer<>(new GenericArmorModel<WizardArmorItem>("wizard")
                .variants(Map.of("hat", IronsSpellbooks.id("geo/wizard_armor_hat.geo.json"))));
    }

    @Override
    public void setupItem(ItemStack stack) {
        if (stack.is(ItemRegistry.WIZARD_HAT.get())) {
            ClothingVariantHelper.setClothingVariant(stack, "hat");
        }
    }
}
