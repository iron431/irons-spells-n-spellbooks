package io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Recipe Type for brewing new liquids in the cauldron based on a base liquid and a reagent item
 */
public record BrewAlchemistCauldronRecipe(FluidStack fluidIn, Ingredient reagent,
                                          List<FluidStack> results,
                                          Optional<ItemStack> byproduct) implements Recipe<BrewAlchemistCauldronRecipe.Input> {

    public static BrewAlchemistCauldronRecipe.Builder builder() {
        return new BrewAlchemistCauldronRecipe.Builder();
    }

    public record Input(FluidStack fluidIn, ItemStack reagent) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return reagent;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    @Override
    public FluidStack fluidIn() {
        return fluidIn.copy();
    }

    @Override
    public List<FluidStack> results() {
        return List.copyOf(this.results);
    }

    @Override
    public Optional<ItemStack> byproduct() {
        return byproduct.map(ItemStack::copy);
    }

    @Override
    public boolean matches(BrewAlchemistCauldronRecipe.Input input, Level level) {
        return FluidStack.isSameFluidSameComponents(fluidIn, input.fluidIn()) && reagent.test(input.reagent());
    }

    @Override
    public ItemStack assemble(BrewAlchemistCauldronRecipe.Input input, HolderLookup.Provider registries) {
        // recipe does not yield items
        return ItemStack.EMPTY.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_BREW_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BrewAlchemistCauldronRecipe> {

        public static final MapCodec<BrewAlchemistCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                FluidStack.CODEC.fieldOf("base_fluid").forGetter(BrewAlchemistCauldronRecipe::fluidIn),
                Ingredient.CODEC.fieldOf("input").forGetter(BrewAlchemistCauldronRecipe::reagent),
                Codec.list(FluidStack.CODEC).fieldOf("results").forGetter(BrewAlchemistCauldronRecipe::results),
                ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("byproduct").forGetter(BrewAlchemistCauldronRecipe::byproduct)
        ).apply(builder, BrewAlchemistCauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BrewAlchemistCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
                FluidStack.STREAM_CODEC, BrewAlchemistCauldronRecipe::fluidIn,
                Ingredient.CONTENTS_STREAM_CODEC, BrewAlchemistCauldronRecipe::reagent,
                ByteBufCodecs.fromCodec(Codec.list(FluidStack.CODEC)), BrewAlchemistCauldronRecipe::results,
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC), BrewAlchemistCauldronRecipe::byproduct,
                BrewAlchemistCauldronRecipe::new
        );


        @Override
        public MapCodec<BrewAlchemistCauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrewAlchemistCauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class Builder implements RecipeBuilder {

        FluidStack input = null;
        Ingredient reagent = null;
        List<FluidStack> results = new ArrayList<>();
        ItemStack byproduct = null;

        public Builder withInput(Holder<Fluid> fluid, int amount) {
            return withInput(new FluidStack(fluid, amount));
        }

        public Builder withInput(FluidStack fluidStack) {
            this.input = fluidStack;
            return this;
        }

        public Builder withReagent(Item item) {
            this.reagent = Ingredient.of(item);
            return this;
        }

        public Builder withReagent(ItemStack item) {
            this.reagent = Ingredient.of(item);
            return this;
        }

        public Builder withReagent(TagKey<Item> item) {
            this.reagent = Ingredient.of(item);
            return this;
        }

        public Builder withResult(FluidStack fluidStack) {
            results.add(fluidStack);
            return this;
        }

        public Builder withResult(Holder<Fluid> fluid, int amount) {
            return withResult(new FluidStack(fluid, amount));
        }

        public Builder withByproduct(ItemStack item) {
            this.byproduct = item;
            return this;
        }

        public Builder withByproduct(Holder<Item> item) {
            return withByproduct(new ItemStack(item));
        }

        public Builder withByproduct(Item item) {
            return withByproduct(new ItemStack(item));
        }

        @Override
        public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
            return this;
        }

        @Override
        public RecipeBuilder group(@Nullable String groupName) {
            return this;
        }

        @Override
        public Item getResult() {
            return Items.AIR;
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
            //todo: validity check results' size, custom throw (instead of null)
            Objects.requireNonNull(input);
            Objects.requireNonNull(reagent);
            recipeOutput.accept(id, new BrewAlchemistCauldronRecipe(input, reagent, results, Optional.ofNullable(byproduct)), null);
        }

        @Override
        public void save(RecipeOutput recipeOutput) {
            save(recipeOutput, BuiltInRegistries.FLUID.getKey(results.getFirst().getFluid()).withPrefix("alchemist_cauldron/brew_"));
        }

        public void saveSoak(RecipeOutput recipeOutput) {
            save(recipeOutput, BuiltInRegistries.ITEM.getKey(byproduct.getItem()).withPrefix("alchemist_cauldron/soak_"));
        }
    }
}
