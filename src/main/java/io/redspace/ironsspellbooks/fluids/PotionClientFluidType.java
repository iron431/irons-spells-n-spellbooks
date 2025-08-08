package io.redspace.ironsspellbooks.fluids;

import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;

public class PotionClientFluidType extends SimpleClientFluidType {
    public PotionClientFluidType(ResourceLocation texture) {
        super(texture);
    }

    @Override
    public int getTintColor(FluidStack stack) {
        return (FluidHelper.hasPotionContents(stack) ? PotionUtils.getColor(FluidHelper.getPotionContents(stack).getEffects()) : PotionUtils.getColor(Potions.WATER)) | 0xFF000000;
    }

}
