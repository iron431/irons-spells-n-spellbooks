package io.redspace.ironsspellbooks.recipe_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class NoAdditionSmithingTransformRecipe implements SmithingRecipe {
    final Ingredient template;

    final Ingredient base;

    final ItemStack result;

    public NoAdditionSmithingTransformRecipe(Ingredient template, Ingredient base, ItemStack result) {
        this.template = template;
        this.base = base;
        this.result = result;
    }

    public boolean matches(SmithingRecipeInput input, Level level) {
        return this.template.test(input.template()) && this.base.test(input.base()) && input.addition().isEmpty();
    }

    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack itemstack = input.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    public Ingredient getTemplate() {
        return template;
    }

    public Ingredient getBase() {
        return base;
    }

    public Ingredient getResult() {
        return Ingredient.of(this.result);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.SMITHING_TRANSFORM_NO_ADDITION_SERIALIZER.get();
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base).anyMatch(Ingredient::hasNoItems);
    }

    public static class Serializer implements RecipeSerializer<NoAdditionSmithingTransformRecipe> {
        private static final MapCodec<NoAdditionSmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_340782_ -> p_340782_.group(
                                Ingredient.CODEC.fieldOf("template").forGetter(p_301310_ -> p_301310_.template),
                                Ingredient.CODEC.fieldOf("base").forGetter(p_300938_ -> p_300938_.base),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_300935_ -> p_300935_.result)
                        )
                        .apply(p_340782_, NoAdditionSmithingTransformRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, NoAdditionSmithingTransformRecipe> STREAM_CODEC = StreamCodec.of(
                NoAdditionSmithingTransformRecipe.Serializer::toNetwork, NoAdditionSmithingTransformRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<NoAdditionSmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NoAdditionSmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static NoAdditionSmithingTransformRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new NoAdditionSmithingTransformRecipe(ingredient, ingredient1, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, NoAdditionSmithingTransformRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
