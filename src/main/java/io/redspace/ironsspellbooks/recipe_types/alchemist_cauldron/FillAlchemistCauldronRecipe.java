package io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Recipe Type for putting liquids into the cauldron (filling cauldron)
 */
public record FillAlchemistCauldronRecipe(Ingredient input, ItemStack returned,
                                          FluidStack result, boolean mustFitAll,
                                          Holder<SoundEvent> fillSound) implements Recipe<SingleRecipeInput> {

    @Override
    public FluidStack result() {
        return result.copy();
    }

    public ItemStack returned() {
        return returned.copy();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return returned.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return returned.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_FILL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FillAlchemistCauldronRecipe> {
        public static final MapCodec<FillAlchemistCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC.fieldOf("input").forGetter(FillAlchemistCauldronRecipe::input),
                ItemStack.CODEC.fieldOf("result").forGetter(FillAlchemistCauldronRecipe::returned),
                FluidStack.CODEC.fieldOf("fluid").forGetter(FillAlchemistCauldronRecipe::result),
                Codec.BOOL.optionalFieldOf("mustFitAll", true).forGetter(FillAlchemistCauldronRecipe::mustFitAll),
                BuiltInRegistries.SOUND_EVENT.holderByNameCodec().optionalFieldOf("sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY)).forGetter(FillAlchemistCauldronRecipe::fillSound)
        ).apply(builder, FillAlchemistCauldronRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FillAlchemistCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, FillAlchemistCauldronRecipe::input,
                ItemStack.STREAM_CODEC, FillAlchemistCauldronRecipe::returned,
                FluidStack.STREAM_CODEC, FillAlchemistCauldronRecipe::result,
                ByteBufCodecs.BOOL, FillAlchemistCauldronRecipe::mustFitAll,
                ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT), FillAlchemistCauldronRecipe::fillSound,
                FillAlchemistCauldronRecipe::new
        );

        @Override
        public MapCodec<FillAlchemistCauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FillAlchemistCauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class Builder implements RecipeBuilder {

        SoundEvent soundEvent = SoundEvents.BOTTLE_EMPTY;
        Ingredient input = null;
        ItemStack returned = null;
        FluidStack fluid = null;
        boolean mustFitAll = true;

        public Builder withInput(Item input) {
            this.input = Ingredient.of(input);
            return this;
        }

        public Builder withInput(Ingredient input){
            this.input = input;
            return this;
        }

        public Builder withReturnItem(Item returned) {
            this.returned = new ItemStack(returned);
            return this;
        }

        public Builder withFluid(Holder<Fluid> fluid, int amount) {
            return withFluid(new FluidStack(fluid, amount));
        }

        public Builder withSound(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }

        public Builder withFluid(FluidStack fluidStack) {
            this.fluid = fluidStack;
            return this;
        }

        public Builder mustFitAll(boolean mustFitAll) {
            this.mustFitAll = mustFitAll;
            return this;
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
            return returned.getItem();
        }

        @Override
        public void save(RecipeOutput recipeOutput) {
            save(recipeOutput, IronsSpellbooks.id("alchemist_cauldron/fill_" + BuiltInRegistries.ITEM.getKey(input.getItems()[0].getItem()).getPath()));
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
            recipeOutput.accept(id, new FillAlchemistCauldronRecipe(input, returned, fluid, mustFitAll, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)), null);
        }
    }
}
