package io.redspace.ironsspellbooks.fluids;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

public class PotionFluid extends NoopFluid {
    public PotionFluid(ForgeFlowingFluid.Properties properties) {
        super(properties);
    }

    public static FluidStack of(int amount, Potion potionContents, PotionFluid.BottleType bottleType) {
        FluidStack fluidStack = new FluidStack(FluidRegistry.POTION_FLUID.get(), amount);
        addPotionToFluidStack(fluidStack, potionContents);
        BottleType.set(fluidStack, bottleType);
        return fluidStack;
    }

    public static FluidStack of(int amount, Holder<Potion> potion, BottleType bottleType) {
        return of(amount, potion.get(), bottleType);
    }

    public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potionContents) {
        if (potionContents == Potions.EMPTY) {
//            fs.remove(DataComponents.POTION_CONTENTS);
            return fs;
        } else {
//            fs.set(DataComponents.POTION_CONTENTS, potionContents);
            FluidHelper.setPotionContents(fs, potionContents);
            return fs;
        }
    }

    public static FluidStack from(ItemStack stack) {
//        if (!stack.has(DataComponents.POTION_CONTENTS)) {
//            return FluidStack.EMPTY;
//        }
        var potion = PotionUtils.getPotion(stack);
        if (potion == Potions.EMPTY) {
            return FluidStack.EMPTY;
        }
        BottleType type = stack.is(Items.LINGERING_POTION) ? BottleType.LINGERING
                : stack.is(Items.SPLASH_POTION) ? BottleType.SPLASH
                : BottleType.REGULAR;
        var fs = new FluidStack(FluidRegistry.POTION_FLUID.get(), 250);
        FluidHelper.setPotionContents(fs, potion);
        BottleType.set(fs, type);
//        fs.set(DataComponents.POTION_CONTENTS, stack.get(DataComponents.POTION_CONTENTS));
//        fs.set(ComponentRegistry.POTION_BOTTLE_TYPE, type);
        return fs;
    }

    public static ItemStack from(FluidStack stack) {
        if (stack.getAmount() < 250 || !(stack.getFluid().is(FluidTags.WATER) || FluidHelper.hasPotionContents(stack))) {
            return ItemStack.EMPTY;
        }
        PotionFluid.BottleType type = BottleType.get(stack);
        Item item = type == BottleType.LINGERING ? Items.LINGERING_POTION
                : type == BottleType.SPLASH ? Items.SPLASH_POTION
                : Items.POTION;
        var is = new ItemStack(item);
//        is.set(DataComponents.POTION_CONTENTS, stack.getOrDefault(DataComponents.POTION_CONTENTS, new Potion(Potions.WATER)));
        PotionUtils.setPotion(is, FluidHelper.hasPotionContents(stack) ? FluidHelper.getPotionContents(stack) : Potions.WATER);
        return is;
    }

    public enum BottleType implements StringRepresentable {
        REGULAR("regular", "potion"),
        SPLASH("splash", "splash_potion"),
        LINGERING("lingering", "lingering_potion");
        final String id, descriptionId;

        public static final Codec<PotionFluid.BottleType> CODEC = StringRepresentable.fromEnum(PotionFluid.BottleType::values);
//        public static final StreamCodec<ByteBuf, PotionFluid.BottleType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        BottleType(String id, String descriptionId) {
            this.id = id;
            this.descriptionId = descriptionId;
        }

        public String descriptionId() {
            return descriptionId;
        }

        public @NotNull String getSerializedName() {
            return id;
        }

        private static final String NBT = "irons_spellbooks:bottle_type";

        public static BottleType get(FluidStack stack) {
            return stack.hasTag() ? valueOf(stack.getOrCreateTag().getString(NBT)) : REGULAR;
        }

        public static void set(FluidStack stack, BottleType type) {
            stack.getOrCreateTag().putString(NBT, type.name());
        }
    }
}
