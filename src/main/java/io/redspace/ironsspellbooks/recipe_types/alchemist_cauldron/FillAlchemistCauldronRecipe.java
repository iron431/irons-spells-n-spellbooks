package io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Recipe Type for putting liquids into the cauldron (filling cauldron)
 */
public record FillAlchemistCauldronRecipe(ResourceLocation getId, Ingredient input, ItemStack returned,
                                          FluidStack result, boolean mustFitAll,
                                          Holder<SoundEvent> fillSound) implements Recipe<Container> {

    @Override
    public FluidStack result() {
        return result.copy();
    }

    public ItemStack returned() {
        return returned.copy();
    }

    @Override
    public boolean matches(Container input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(Container input, RegistryAccess registries) {
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
    public ItemStack getResultItem(RegistryAccess registries) {
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
//        public static final MapCodec<FillAlchemistCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
//                Ingredient.CODEC.fieldOf("input").forGetter(FillAlchemistCauldronRecipe::input),
//                ItemStack.CODEC.fieldOf("result").forGetter(FillAlchemistCauldronRecipe::returned),
//                FluidStack.CODEC.fieldOf("fluid").forGetter(FillAlchemistCauldronRecipe::result),
//                Codec.BOOL.optionalFieldOf("mustFitAll", true).forGetter(FillAlchemistCauldronRecipe::mustFitAll),
//                BuiltInRegistries.SOUND_EVENT.holderByNameCodec().optionalFieldOf("sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY)).forGetter(FillAlchemistCauldronRecipe::fillSound)
//        ).apply(builder, FillAlchemistCauldronRecipe::new));

        @Override
        public FillAlchemistCauldronRecipe fromJson(ResourceLocation id, JsonObject recipejson) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(recipejson, "input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(recipejson, "result"));
            FluidStack fluid = FluidStack.CODEC.decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "fluid")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            boolean mustFitAll = GsonHelper.getAsBoolean(recipejson, "mustFitAll", true);
            Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY);
            if (recipejson.has("sound")) {
                sound = BuiltInRegistries.SOUND_EVENT.holderByNameCodec().decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "sound")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            }
            return new FillAlchemistCauldronRecipe(id, input, result, fluid, mustFitAll, sound);
        }

        @Override
        public @Nullable FillAlchemistCauldronRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            FluidStack fluid = FluidStack.readFromPacket(buf);
            boolean mustFitAll = buf.readBoolean();
            Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation())));
            return new FillAlchemistCauldronRecipe(pRecipeId, input, result, fluid, mustFitAll, sound);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FillAlchemistCauldronRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.returned);
            recipe.result.writeToPacket(buf);
            buf.writeBoolean(recipe.mustFitAll);
            buf.writeResourceLocation(recipe.fillSound.get().getLocation());
        }
//        public static final StreamCodec<RegistryFriendlyByteBuf, FillAlchemistCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
//                Ingredient.CONTENTS_STREAM_CODEC, FillAlchemistCauldronRecipe::input,
//                ItemStack.STREAM_CODEC, FillAlchemistCauldronRecipe::returned,
//                FluidStack.STREAM_CODEC, FillAlchemistCauldronRecipe::result,
//                ByteBufCodecs.BOOL, FillAlchemistCauldronRecipe::mustFitAll,
//                ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT), FillAlchemistCauldronRecipe::fillSound,
//                FillAlchemistCauldronRecipe::new
//        );

//        @Override
//        public MapCodec<FillAlchemistCauldronRecipe> codec() {
//            return CODEC;
//        }
//
//        @Override
//        public StreamCodec<RegistryFriendlyByteBuf, FillAlchemistCauldronRecipe> streamCodec() {
//            return STREAM_CODEC;
//        }
    }

//    public static class Builder implements RecipeBuilder {
//
//        SoundEvent soundEvent = SoundEvents.BOTTLE_EMPTY;
//        Ingredient input = null;
//        ItemStack returned = null;
//        FluidStack fluid = null;
//        boolean mustFitAll = true;
//
//        public Builder withInput(Item input) {
//            this.input = Ingredient.of(input);
//            return this;
//        }
//
//        public Builder withReturnItem(Item returned) {
//            this.returned = new ItemStack(returned);
//            return this;
//        }
//
//        public Builder withFluid(Holder<Fluid> fluid, int amount) {
//            return withFluid(new FluidStack(fluid, amount));
//        }
//
//        public Builder withSound(SoundEvent soundEvent) {
//            this.soundEvent = soundEvent;
//            return this;
//        }
//
//        public Builder withFluid(FluidStack fluidStack) {
//            this.fluid = fluidStack;
//            return this;
//        }
//
//        public Builder mustFitAll(boolean mustFitAll) {
//            this.mustFitAll = mustFitAll;
//            return this;
//        }
//
//        @Override
//        public RecipeBuilder group(@Nullable String groupName) {
//            return this;
//        }
//
//        @Override
//        public Item getResult() {
//            return returned.getItem();
//        }
//
//        @Override
//        public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
//            pFinishedRecipeConsumer.accept();
//        }
//
//        @Override
//        public void save(RecipeOutput recipeOutput) {
//            save(recipeOutput, IronsSpellbooks.id("alchemist_cauldron/fill_" + BuiltInRegistries.ITEM.getKey(input.getItems()[0].getItem()).getPath()));
//        }
//
//        @Override
//        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
//            recipeOutput.accept(id, new FillAlchemistCauldronRecipe(input, returned, fluid, mustFitAll, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)), null);
//        }
//    }
}
