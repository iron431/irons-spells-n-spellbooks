package io.redspace.ironsspellbooks.fluids;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public class PotionFluidType extends FluidType {
    /**
     * Default constructor.
     *
     * @param properties the general properties of the fluid type
     */
    public PotionFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(FluidStack stack) {
        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        var bottle = stack.getOrDefault(ComponentRegistry.POTION_BOTTLE_TYPE, PotionFluid.BottleType.REGULAR);
        if (potionContents != null) {
            return Potion.getName(potionContents.potion(), String.format("item.minecraft.%s.effect.", bottle.descriptionId()));
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public Component getDescription(FluidStack stack) {
        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            if (potionContents.hasEffects()) {
                var effects = potionContents.getAllEffects();
                var primary = effects.iterator().next();
                if (primary.getAmplifier() > 0) {
                    return Component.translatable(this.getDescriptionId(stack)).append(" " + simpleRomanNumeral(primary.getAmplifier() + 1));

                }
            }

        }
        return super.getDescription(stack);
    }

    private String simpleRomanNumeral(int i) {
        return switch (i) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(i);
        };
    }
}
