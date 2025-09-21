package io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Recipe Type for taking liquids out of the cauldron (emptying cauldron)
 */
public record EmptyAlchemistCauldronRecipe(ResourceLocation getId, Ingredient input, ItemStack result,
                                           FluidStack fluid,
                                           Holder<SoundEvent> emptySound) implements Recipe<EmptyAlchemistCauldronRecipe.Input> {
    public record Input(ItemStack item, FluidStack fluid) implements Container {
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
            return item;
        }

        @Override
        public ItemStack removeItem(int pSlot, int pAmount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int pSlot) {
            return ItemStack.EMPTY;
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

    public ItemStack result() {
        return result.copy();
    }

    @Override
    public FluidStack fluid() {
        return fluid.copy();
    }

    @Override
    public boolean matches(EmptyAlchemistCauldronRecipe.Input input, Level level) {
        return this.input.test(input.item()) && input.fluid.getAmount() >= this.fluid.getAmount() && FluidHelper.isSameFluidSameComponents(this.fluid, input.fluid);
    }

    @Override
    public ItemStack assemble(Input pContainer, RegistryAccess pRegistryAccess) {
        return result.copy();
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
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<EmptyAlchemistCauldronRecipe> {
        @Override
        public EmptyAlchemistCauldronRecipe fromJson(ResourceLocation id, JsonObject recipejson) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(recipejson, "input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(recipejson, "result"));
            FluidStack fluid = FluidStack.CODEC.decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "fluid")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY);
            if (recipejson.has("sound")) {
                sound = BuiltInRegistries.SOUND_EVENT.holderByNameCodec().decode(JsonOps.INSTANCE, GsonHelper.getNonNull(recipejson, "sound")).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
            }
            return new EmptyAlchemistCauldronRecipe(id, input, result, fluid, sound);
        }

        @Override
        public @Nullable EmptyAlchemistCauldronRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            FluidStack fluid = FluidStack.readFromPacket(buf);
            Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation())));
            return new EmptyAlchemistCauldronRecipe(pRecipeId, input, result, fluid, sound);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, EmptyAlchemistCauldronRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.result);
            recipe.fluid.writeToPacket(buf);
            buf.writeResourceLocation(recipe.emptySound.get().getLocation());
        }
//        public static final MapCodec<EmptyAlchemistCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
//                Ingredient.CODEC.fieldOf("input").forGetter(EmptyAlchemistCauldronRecipe::input),
//                ItemStack.CODEC.fieldOf("result").forGetter(EmptyAlchemistCauldronRecipe::result),
//                FluidStack.CODEC.fieldOf("fluid").forGetter(EmptyAlchemistCauldronRecipe::fluid),
//                BuiltInRegistries.SOUND_EVENT.holderByNameCodec().optionalFieldOf("sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_FILL)).forGetter(EmptyAlchemistCauldronRecipe::emptySound)
//        ).apply(builder, EmptyAlchemistCauldronRecipe::new));
//        public static final StreamCodec<RegistryFriendlyByteBuf, EmptyAlchemistCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
//                Ingredient.CONTENTS_STREAM_CODEC, EmptyAlchemistCauldronRecipe::input,
//                ItemStack.STREAM_CODEC, EmptyAlchemistCauldronRecipe::result,
//                FluidStack.STREAM_CODEC, EmptyAlchemistCauldronRecipe::fluid,
//                ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT), EmptyAlchemistCauldronRecipe::emptySound,
//                EmptyAlchemistCauldronRecipe::new
//        );
//
//        @Override
//        public MapCodec<EmptyAlchemistCauldronRecipe> codec() {
//            return CODEC;
//        }
//
//        @Override
//        public StreamCodec<RegistryFriendlyByteBuf, EmptyAlchemistCauldronRecipe> streamCodec() {
//            return STREAM_CODEC;
//        }
    }

//    public static class Builder implements RecipeBuilder {
//
//        SoundEvent soundEvent = SoundEvents.BOTTLE_FILL;
//        Ingredient input = null;
//        ItemStack returned = null;
//        FluidStack fluid = null;
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
//        @Override
//        public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
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
//        public void save(RecipeOutput recipeOutput) {
//            recipeOutput.accept(IronsSpellbooks.id("alchemist_cauldron/empty_" + BuiltInRegistries.ITEM.getKey(returned.getItem()).getPath()), new EmptyAlchemistCauldronRecipe(input, returned, fluid, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)), null);
//        }
//
//        @Override
//        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
//            recipeOutput.accept(id, new EmptyAlchemistCauldronRecipe(input, returned, fluid, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)), null);
//        }
//    }
}
