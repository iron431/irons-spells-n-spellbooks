package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class VanillaAnvilRecipeMaker {

    public static List<IJeiAnvilRecipe> getAnvilRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
        return Stream.concat(
                getArmorRepairRecipes(vanillaRecipeFactory),
                getItemRepairRecipes(vanillaRecipeFactory)
        ).toList();
    }

    public static Stream<IJeiAnvilRecipe> getItemRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
        var repairableItems = getTieredItems();
        return repairableItems.stream()
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

    public static Stream<IJeiAnvilRecipe> getArmorRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
        var repairableItems = getArmorItems();
        return repairableItems.stream()
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

    public static List<TieredItem> getTieredItems() {
        var registryItems = CreativeTabRegistry.EQUIPMENT_TAB.get().getSearchTabDisplayItems();
        List<TieredItem> items = new ArrayList<>();
        for (ItemStack item : registryItems)
            if (item.getItem() instanceof TieredItem tieredItem)
                items.add(tieredItem);
        return items;
    }

    public static List<ArmorItem> getArmorItems() {
        var registryItems = CreativeTabRegistry.EQUIPMENT_TAB.get().getSearchTabDisplayItems();
        List<ArmorItem> items = new ArrayList<>();
        for (ItemStack item : registryItems)
            if (item.getItem() instanceof ArmorItem tieredItem)
                items.add(tieredItem);
        return items;
    }
}
