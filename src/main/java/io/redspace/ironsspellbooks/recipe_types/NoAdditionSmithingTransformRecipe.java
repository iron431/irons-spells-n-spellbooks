//package io.redspace.ironsspellbooks.recipe_types;
//
//import com.google.gson.JsonObject;
//import io.redspace.ironsspellbooks.registries.RecipeRegistry;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.Container;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.*;
//import net.minecraft.world.level.Level;
//
//import java.util.stream.Stream;
//
//public class NoAdditionSmithingTransformRecipe implements SmithingRecipe {
//    final Ingredient template;
//
//    final Ingredient base;
//
//    final ItemStack result;
//    final ResourceLocation id;
//
//    public NoAdditionSmithingTransformRecipe(ResourceLocation p_267117_, Ingredient template, Ingredient base, ItemStack result) {
//        id = p_267117_;
//        this.template = template;
//        this.base = base;
//        this.result = result;
//    }
//
//    public boolean matches(Container input, Level level) {
//        return this.template.test(input.getItem(0)) && this.base.test(input.getItem(1)) && input.getItem(2).isEmpty();
//    }
//
//    //    public ItemStack assemble(Container input, HolderLookup.Provider registries) {
////        ItemStack itemstack = input.base().transmuteCopy(this.result.getItem(), this.result.getCount());
////        itemstack.applyComponents(this.result.getComponentsPatch());
////        return itemstack;
////    }
//    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
//        ItemStack itemstack = this.result.copy();
//        CompoundTag compoundtag = pContainer.getItem(1).getTag();
//        if (compoundtag != null) {
//            itemstack.setTag(compoundtag.copy());
//        }
//
//        return itemstack;
//    }
//
//    @Override
//    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
//        return result.copy();
//    }
//
//    @Override
//    public ResourceLocation getId() {
//        return id;
//    }
//
//    public Ingredient getTemplate() {
//        return template;
//    }
//
//    public Ingredient getBase() {
//        return base;
//    }
//
//    public Ingredient getResult() {
//        return Ingredient.of(this.result);
//    }
//
////    @Override
////    public ItemStack getResultItem(HolderLookup.Provider registries) {
////        return this.result;
////    }
//
//    @Override
//    public boolean isTemplateIngredient(ItemStack stack) {
//        return this.template.test(stack);
//    }
//
//    @Override
//    public boolean isBaseIngredient(ItemStack stack) {
//        return this.base.test(stack);
//    }
//
//    @Override
//    public boolean isAdditionIngredient(ItemStack stack) {
//        return false;
//    }
//
//    @Override
//    public RecipeSerializer<?> getSerializer() {
//        return RecipeRegistry.SMITHING_TRANSFORM_NO_ADDITION_SERIALIZER.get();
//    }
//
//    @Override
//    public boolean isIncomplete() {
//        return Stream.of(this.template, this.base).anyMatch(Ingredient::isEmpty);
//    }
//
//    public static class Serializer implements RecipeSerializer<NoAdditionSmithingTransformRecipe> {
//        public NoAdditionSmithingTransformRecipe fromJson(ResourceLocation p_266953_, JsonObject p_266720_) {
//            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "template"));
//            Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "base"));
////            Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "addition"));
//            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(p_266720_, "result"));
//            return new NoAdditionSmithingTransformRecipe(p_266953_, ingredient, ingredient1, itemstack);
//        }
//
//        public NoAdditionSmithingTransformRecipe fromNetwork(ResourceLocation p_267117_, FriendlyByteBuf p_267316_) {
//            Ingredient ingredient = Ingredient.fromNetwork(p_267316_);
//            Ingredient ingredient1 = Ingredient.fromNetwork(p_267316_);
////            Ingredient ingredient2 = Ingredient.fromNetwork(p_267316_);
//            ItemStack itemstack = p_267316_.readItem();
//            return new NoAdditionSmithingTransformRecipe(p_267117_, ingredient, ingredient1, itemstack);
//        }
//
//        public void toNetwork(FriendlyByteBuf p_266746_, NoAdditionSmithingTransformRecipe p_266927_) {
//            p_266927_.template.toNetwork(p_266746_);
//            p_266927_.base.toNetwork(p_266746_);
////            p_266927_.addition.toNetwork(p_266746_);
//            p_266746_.writeItem(p_266927_.result);
//        }
//    }
//}
