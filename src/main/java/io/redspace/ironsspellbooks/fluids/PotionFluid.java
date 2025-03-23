package io.redspace.ironsspellbooks.fluids;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class PotionFluid extends NoopFluid {
    public PotionFluid(BaseFlowingFluid.Properties properties) {
        super(properties);
    }

    public static FluidStack of(int amount, PotionContents potionContents, PotionFluid.BottleType bottleType) {
        FluidStack fluidStack = new FluidStack(FluidRegistry.POTION_FLUID, amount);
        addPotionToFluidStack(fluidStack, potionContents);
        fluidStack.set(ComponentRegistry.POTION_BOTTLE_TYPE, bottleType);
        return fluidStack;
    }

    public static FluidStack of(int amount, Holder<Potion> potion, BottleType bottleType) {
        return of(amount, new PotionContents(potion), bottleType);
    }

    public static FluidStack addPotionToFluidStack(FluidStack fs, PotionContents potionContents) {
        if (potionContents == PotionContents.EMPTY) {
            fs.remove(DataComponents.POTION_CONTENTS);
            return fs;
        } else {
            fs.set(DataComponents.POTION_CONTENTS, potionContents);
            return fs;
        }
    }

    public static FluidStack from(ItemStack stack) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) {
            return FluidStack.EMPTY;
        }
        BottleType type = stack.is(Items.LINGERING_POTION) ? BottleType.LINGERING
                : stack.is(Items.SPLASH_POTION) ? BottleType.SPLASH
                : BottleType.REGULAR;
        var fs = new FluidStack(FluidRegistry.POTION_FLUID, 250);
        fs.set(DataComponents.POTION_CONTENTS, stack.get(DataComponents.POTION_CONTENTS));
        fs.set(ComponentRegistry.POTION_BOTTLE_TYPE, type);
        return fs;
    }

    public static ItemStack from(FluidStack stack) {
        if (stack.getAmount() < 250 || !(stack.is(Tags.Fluids.WATER) || stack.has(DataComponents.POTION_CONTENTS))) {
            return ItemStack.EMPTY;
        }
        PotionFluid.BottleType type = stack.getOrDefault(ComponentRegistry.POTION_BOTTLE_TYPE, PotionFluid.BottleType.REGULAR);
        Item item = type == BottleType.LINGERING ? Items.LINGERING_POTION
                : type == BottleType.SPLASH ? Items.SPLASH_POTION
                : Items.POTION;
        var is = new ItemStack(item);
        is.set(DataComponents.POTION_CONTENTS, stack.getOrDefault(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER)));
        return is;
    }

    public enum BottleType implements StringRepresentable {
        REGULAR("regular", "potion"),
        SPLASH("splash", "splash_potion"),
        LINGERING("lingering", "lingering_potion");
        final String id, descriptionId;

        public static final Codec<PotionFluid.BottleType> CODEC = StringRepresentable.fromEnum(PotionFluid.BottleType::values);
        public static final StreamCodec<ByteBuf, PotionFluid.BottleType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

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
    }
}
