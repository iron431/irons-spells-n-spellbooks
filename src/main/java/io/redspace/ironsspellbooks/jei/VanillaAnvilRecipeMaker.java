package io.redspace.ironsspellbooks.jei;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class VanillaAnvilRecipeMaker {

    static List<IJeiAnvilRecipe> getAnvilRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        return Stream.concat(
                getArmorRepairRecipes(vanillaRecipeFactory, itemFinder),
                getItemRepairRecipes(vanillaRecipeFactory, itemFinder)
        ).toList();
    }

//    static List<RecipeHolder<SmithingRecipe>> getCustomSmithingRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
//        if (Minecraft.getInstance().level == null) {
//            return List.of();
//        }
//        return Minecraft.getInstance().level.getRecipeManager()
//                .getAllRecipesFor(RecipeType.SMITHING).stream()
//                .filter(holder -> holder.value() instanceof NoAdditionSmithingTransformRecipe).toList();
//    }

    static Stream<IJeiAnvilRecipe> getItemRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        return itemFinder.ironsTieredItems.stream()
                .mapMulti((item, consumer) -> {
                    ItemStack damagedThreeQuarters = new ItemStack(item);
                    damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
                    ItemStack damagedHalf = new ItemStack(item);
                    damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

                    IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedThreeQuarters), List.of(damagedThreeQuarters), List.of(damagedHalf));
                    consumer.accept(repairWithSame);

                    List<ItemStack> repairMaterials = Arrays.stream(item.getTier().getRepairIngredient().getItems()).toList();
                    ItemStack damagedFully = new ItemStack(item);
                    damagedFully.setDamageValue(damagedFully.getMaxDamage());
                    IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedFully), repairMaterials, List.of(damagedThreeQuarters));
                    consumer.accept(repairWithMaterial);
                });
    }

    static Stream<IJeiAnvilRecipe> getArmorRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        return itemFinder.ironsArmorItems.stream()
                .mapMulti((item, consumer) -> {
                    ItemStack damagedThreeQuarters = new ItemStack(item);
                    damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
                    ItemStack damagedHalf = new ItemStack(item);
                    damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

                    IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedThreeQuarters), List.of(damagedThreeQuarters), List.of(damagedHalf));
                    consumer.accept(repairWithSame);

                    List<ItemStack> repairMaterials = Arrays.stream(item.getMaterial().getRepairIngredient().getItems()).toList();
                    ItemStack damagedFully = new ItemStack(item);
                    damagedFully.setDamageValue(damagedFully.getMaxDamage());
                    IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedFully), repairMaterials, List.of(damagedThreeQuarters));
                    consumer.accept(repairWithMaterial);
                });
    }
}
