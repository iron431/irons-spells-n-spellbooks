package io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Recipe Type for brewing new liquids in the cauldron based on a base liquid and a reagent item
 */
public record BrewAlchemistCauldronRecipe(ResourceLocation id, FluidStack fluidIn, Ingredient reagent,
                                          List<FluidStack> results,
                                          Optional<ItemStack> byproduct) implements Recipe<BrewAlchemistCauldronRecipe.Input> {

//    public static BrewAlchemistCauldronRecipe.Builder builder() {
//        return new BrewAlchemistCauldronRecipe.Builder();
//    }

    public record Input(FluidStack fluidIn, ItemStack reagent) implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int index) {
            return reagent;
        }

        @Override
        public ItemStack removeItem(int pSlot, int pAmount) {
            return null;
        }

        @Override
        public ItemStack removeItemNoUpdate(int pSlot) {
            return null;
        }

        @Override
        public void setItem(int pSlot, ItemStack pStack) {

        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player pPlayer) {
            return false;
        }

        @Override
        public void clearContent() {

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
        return FluidHelper.isSameFluidSameComponents(fluidIn, input.fluidIn()) && reagent.test(input.reagent());
    }

    @Override
    public ItemStack assemble(BrewAlchemistCauldronRecipe.Input input, RegistryAccess registries) {
        // recipe does not yield items
        return ItemStack.EMPTY.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
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

//        public static final MapCodec<BrewAlchemistCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
//                FluidStack.CODEC.fieldOf("base_fluid").forGetter(BrewAlchemistCauldronRecipe::fluidIn),
//                Ingredient.CODEC.fieldOf("input").forGetter(BrewAlchemistCauldronRecipe::reagent),
//                Codec.list(FluidStack.CODEC).fieldOf("results").forGetter(BrewAlchemistCauldronRecipe::results),
//                ItemStack.CODEC.optionalFieldOf("byproduct").forGetter(BrewAlchemistCauldronRecipe::byproduct)
//        ).apply(builder, BrewAlchemistCauldronRecipe::new));

//        public static final StreamCodec<RegistryFriendlyByteBuf, BrewAlchemistCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
//                FluidStack.STREAM_CODEC, BrewAlchemistCauldronRecipe::fluidIn,
//                Ingredient.CONTENTS_STREAM_CODEC, BrewAlchemistCauldronRecipe::reagent,
//                ByteBufCodecs.fromCodec(Codec.list(FluidStack.CODEC)), BrewAlchemistCauldronRecipe::results,
//                ByteBufCodecs.optional(ItemStack.STREAM_CODEC), BrewAlchemistCauldronRecipe::byproduct,
//                BrewAlchemistCauldronRecipe::new
//        );


//        @Override
//        public MapCodec<BrewAlchemistCauldronRecipe> codec() {
//            return CODEC;
//        }
//
//        @Override
//        public StreamCodec<RegistryFriendlyByteBuf, BrewAlchemistCauldronRecipe> streamCodec() {
//            return STREAM_CODEC;
//        }

        @Override
        public BrewAlchemistCauldronRecipe fromJson(ResourceLocation pRecipeId, JsonObject recipejson) {
            FluidStack baseFluid = FluidStack.CODEC.decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "base_fluid")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(recipejson, "input"));
            List<FluidStack> results = Codec.list(FluidStack.CODEC).decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "results")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            Optional<ItemStack> byproduct = Optional.empty();
            if (recipejson.has("byproduct")) {
                byproduct = Optional.of(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(recipejson, "byproduct")));
            }
            return new BrewAlchemistCauldronRecipe(pRecipeId, baseFluid, input, results, byproduct);
        }

        @Override
        public @Nullable BrewAlchemistCauldronRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf buf) {
            FluidStack baseFluid = FluidStack.readFromPacket(buf);
            Ingredient input = Ingredient.fromNetwork(buf);
            int i = buf.readInt();
            List<FluidStack> results = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                results.add(FluidStack.readFromPacket(buf));
            }
            Optional<ItemStack> byproduct = Optional.empty();
            if (buf.readBoolean()) {
                byproduct = Optional.of(buf.readItem());
            }
            return new BrewAlchemistCauldronRecipe(pRecipeId, baseFluid, input, results, byproduct);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BrewAlchemistCauldronRecipe recipe) {
            recipe.fluidIn.writeToPacket(buf);
            recipe.reagent.toNetwork(buf);
            buf.writeInt(recipe.results.size());
            for (FluidStack result : recipe.results) {
                result.writeToPacket(buf);
            }
            buf.writeBoolean(recipe.byproduct.isPresent());
            if (recipe.byproduct.isPresent()) {
                buf.writeItem(recipe.byproduct.get());
            }
        }
    }

//    public static class Builder implements RecipeBuilder {
//
//        FluidStack input = null;
//        Ingredient reagent = null;
//        List<FluidStack> results = new ArrayList<>();
//        ItemStack byproduct = null;
//
//        public Builder withInput(Holder<Fluid> fluid, int amount) {
//            return withInput(new FluidStack(fluid.get(), amount));
//        }
//
//        public Builder withInput(FluidStack fluidStack) {
//            this.input = fluidStack;
//            return this;
//        }
//
//        public Builder withReagent(Item item) {
//            this.reagent = Ingredient.of(item);
//            return this;
//        }
//
//        public Builder withReagent(ItemStack item) {
//            this.reagent = Ingredient.of(item);
//            return this;
//        }
//
//        public Builder withReagent(TagKey<Item> item) {
//            this.reagent = Ingredient.of(item);
//            return this;
//        }
//
//        public Builder withResult(FluidStack fluidStack) {
//            results.add(fluidStack);
//            return this;
//        }
//
//        public Builder withResult(Holder<Fluid> fluid, int amount) {
//            return withResult(new FluidStack(fluid, amount));
//        }
//
//        public Builder withByproduct(ItemStack item) {
//            this.byproduct = item;
//            return this;
//        }
//
//        public Builder withByproduct(Holder<Item> item) {
//            return withByproduct(new ItemStack(item));
//        }
//
//        public Builder withByproduct(Item item) {
//            return withByproduct(new ItemStack(item));
//        }
//
//        @Override
//        public RecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
//            return null;
//        }
//
//        @Override
//        public RecipeBuilder group(@Nullable String pGroupName) {
//            return null;
//        }
//
//        @Override
//        public Item getResult() {
//            return null;
//        }
//
//        @Override
//        public void save(Consumer<FinishedRecipe> recipeOutput, ResourceLocation id) {
//            //todo: validity check results' size, custom throw (instead of null)
//            Objects.requireNonNull(input);
//            Objects.requireNonNull(reagent);
//            recipeOutput.accept(new BrewAlchemistCauldronRecipe(id, input, reagent, results, Optional.ofNullable(byproduct)));
//        }
//
////        @Override
////        public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
////            return this;
////        }
////
////        @Override
////        public RecipeBuilder group(@Nullable String groupName) {
////            return this;
////        }
////
////        @Override
////        public Item getResult() {
////            return Items.AIR;
////        }
////
////        @Override
////        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
////            //todo: validity check results' size, custom throw (instead of null)
////            Objects.requireNonNull(input);
////            Objects.requireNonNull(reagent);
////            recipeOutput.accept(id, new BrewAlchemistCauldronRecipe(input, reagent, results, Optional.ofNullable(byproduct)), null);
////        }
////
////        @Override
////        public void save(RecipeOutput recipeOutput) {
////            save(recipeOutput, BuiltInRegistries.FLUID.getKey(results.getFirst().getFluid()).withPrefix("alchemist_cauldron/brew_"));
////        }
////
////        public void saveSoak(RecipeOutput recipeOutput) {
////            save(recipeOutput, BuiltInRegistries.ITEM.getKey(byproduct.getItem()).withPrefix("alchemist_cauldron/soak_"));
////        }
//    }
}
