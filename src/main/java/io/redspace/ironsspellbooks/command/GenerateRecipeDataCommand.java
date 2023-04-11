package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateRecipeDataCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.generate_recipe_data.failed"));

    private static final String RECIPE_DATA_TEMPLATE = """
            - name: "%s"
              path: "%s"
              item0: "%s"
              item0Path: "%s"
              item1: "%s"
              item1Path: "%s"
              item2: "%s"
              item2Path: "%s"
              item3: "%s"
              item3Path: "%s"
              item4: "%s"
              item4Path: "%s"
              item5: "%s"
              item5Path: "%s"
              item6: "%s"
              item6Path: "%s"
              item7: "%s"
              item7Path: "%s"
              item8: "%s"
              item8Path: "%s"
              description: ""
              
                    """;

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("generateRecipeData").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).executes((commandContext) -> {
            return generateRecipeData(commandContext.getSource());
        }));
    }

    private static int generateRecipeData(CommandSourceStack source) throws CommandSyntaxException {
        try {
            var sb = new StringBuilder();

            Minecraft.getInstance().level.getRecipeManager().getRecipes()
                    .stream()
                    .filter(r -> r.getId().getNamespace().equals("irons_spellbooks"))
                    .forEach(recipe -> {
                        //IronsSpellbooks.LOGGER.debug("recipe: {}", recipe.getId());
                        //IronsSpellbooks.LOGGER.debug("recipe: resultItem: {}", ForgeRegistries.ITEMS.getKey(recipe.getResultItem().getItem()));

                        var resultItemResourceLocation = ForgeRegistries.ITEMS.getKey(recipe.getResultItem().getItem());
                        var recipeData = new ArrayList<RecipeData>(10);
                        recipeData.add(new RecipeData(recipe.getResultItem().getItem().getName(ItemStack.EMPTY).getString(),
                                String.format("/img/items/%s.png", resultItemResourceLocation.getPath())));

                        recipe.getIngredients().forEach(ingredient -> {
                            Arrays.stream(ingredient.getItems())
                                    .findFirst()
                                    .ifPresentOrElse(itemStack -> {
                                        var itemResource = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
                                        var path = "";

                                        if (itemResource.toString().contains("irons_spellbooks")) {
                                            path = String.format("/img/items/%s.png", itemResource.getPath());
                                        } else {
                                            path = String.format("/img/items/minecraft/%s.png", itemResource.getPath());
                                        }

                                        recipeData.add(new RecipeData(
                                                itemStack.getItem().getName(ItemStack.EMPTY).getString(),
                                                path));
                                    }, () -> {
                                        recipeData.add(RecipeData.EMPTY);
                                    });
                        });

                        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                                getRecipeDataAtIndex(recipeData, 0).name,
                                getRecipeDataAtIndex(recipeData, 0).path,
                                getRecipeDataAtIndex(recipeData, 1).name,
                                getRecipeDataAtIndex(recipeData, 1).path,
                                getRecipeDataAtIndex(recipeData, 2).name,
                                getRecipeDataAtIndex(recipeData, 2).path,
                                getRecipeDataAtIndex(recipeData, 3).name,
                                getRecipeDataAtIndex(recipeData, 3).path,
                                getRecipeDataAtIndex(recipeData, 4).name,
                                getRecipeDataAtIndex(recipeData, 4).path,
                                getRecipeDataAtIndex(recipeData, 5).name,
                                getRecipeDataAtIndex(recipeData, 5).path,
                                getRecipeDataAtIndex(recipeData, 6).name,
                                getRecipeDataAtIndex(recipeData, 6).path,
                                getRecipeDataAtIndex(recipeData, 7).name,
                                getRecipeDataAtIndex(recipeData, 7).path,
                                getRecipeDataAtIndex(recipeData, 8).name,
                                getRecipeDataAtIndex(recipeData, 8).path,
                                getRecipeDataAtIndex(recipeData, 9).name,
                                getRecipeDataAtIndex(recipeData, 9).path
                        ));
                    });

            var file = new BufferedWriter(new FileWriter("recipe_data.yml"));
            file.write(sb.toString());
            file.close();
            return 1;
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.debug(e.getMessage());
        }

        throw ERROR_FAILED.create();
    }

    private static RecipeData getRecipeDataAtIndex(List<RecipeData> recipeData, int index) {
        if (index < recipeData.size()) {
            return recipeData.get(index);
        } else {
            return RecipeData.EMPTY;
        }
    }

    private record RecipeData(String name, String path) {
        public static RecipeData EMPTY = new RecipeData("", "");

    }
}
