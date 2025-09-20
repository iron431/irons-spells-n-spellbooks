package io.redspace.ironsspellbooks.fluids;

import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectUtil;
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
        Potion potion = FluidHelper.getPotionContents(stack);
        PotionFluid.BottleType bottle = PotionFluid.BottleType.get(stack);
        return potion.getName(String.format("item.minecraft.%s.effect.", bottle.descriptionId()));

//        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
//        var bottle = stack.getOrDefault(ComponentRegistry.POTION_BOTTLE_TYPE, PotionFluid.BottleType.REGULAR);
//        if (potionContents != null) {
//            return Potion.getName(potionContents.potion(), String.format("item.minecraft.%s.effect.", bottle.descriptionId()));
//        }
//        return super.getDescriptionId(stack);
    }

    @Override
    public Component getDescription(FluidStack stack) {
//        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        Potion potion = FluidHelper.getPotionContents(stack);
        if (potion != null) {
            if (!potion.getEffects().isEmpty()) {
                var effects = potion.getEffects();
                var primary = effects.iterator().next();
                MutableComponent component = Component.translatable(this.getDescriptionId(stack));
                if (primary.getAmplifier() > 0) {
                    component = component.append(" " + simpleRomanNumeral(primary.getAmplifier() + 1));
                }
                if (!primary.getEffect().isInstantenous() && primary.getDuration() > 0) {
                    component = component.append(String.format(" (%s)", MobEffectUtil.formatDuration(primary, 1f).getString()));
                }
                return component;
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
