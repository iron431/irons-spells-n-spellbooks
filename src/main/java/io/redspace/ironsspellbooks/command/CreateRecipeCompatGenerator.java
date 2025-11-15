package io.redspace.ironsspellbooks.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.io.File;
import java.io.FileWriter;

public class CreateRecipeCompatGenerator {

    public static int run(CommandContext<CommandSourceStack> context) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        new File("create_compat").mkdir();
        recipeManager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get()).stream()
                .forEach(
                        recipe -> {
//                            var recipe = recipeHolder.value();
                            if (recipe.result().getItemHolder().unwrapKey().get().location().getNamespace().equals("irons_spellbooks")) {
                                String stringJson = String.format(FILL_FORMAT,
//                                    Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, recipe.input()).getOrThrow().toString(),
                                        recipe.input().toJson().toString(),
                                        recipe.fluid().getFluid().builtInRegistryHolder().unwrapKey().get().location().toString(),
                                        recipe.fluid().getAmount(),
                                        recipe.result().getItemHolder().unwrapKey().get().location().toString()
                                );
                                String outputFilepath = String.format("create_compat/create_fill_%s.json", recipe.getId().getPath().split("/", 2)[1].split("_", 2)[1]);
                                var file = new File(outputFilepath);
                                try (FileWriter writer = new FileWriter(file)) {
                                    writer.write(stringJson);
                                } catch (Exception e) {
                                    IronsSpellbooks.LOGGER.debug("Failed to generate recipe \"{}\": {}", outputFilepath, e.getMessage());
                                }
                            }
                        }
                );
        recipeManager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get()).stream()
                .forEach(
                        recipe -> {
//                            var recipe = recipeHolder.value();
                            if (recipe.result().getFluid().builtInRegistryHolder().unwrapKey().get().location().getNamespace().equals("irons_spellbooks")) {
                                String stringJson = String.format(EMPTY_FORMAT,
//                                    Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, recipe.input()).getOrThrow().toString(),
                                        recipe.input().toJson().toString(),
                                        recipe.returned().getItemHolder().unwrapKey().get().location().toString(),
                                        recipe.result().getFluid().builtInRegistryHolder().unwrapKey().get().location().toString(),
                                        recipe.result().getAmount()
                                );
                                new File("create_compat").mkdir();
                                String outputFilepath = String.format("create_compat/create_empty_%s.json", recipe.getId().getPath().split("/", 2)[1].split("_", 2)[1]);
                                var file = new File(outputFilepath);
                                try (FileWriter writer = new FileWriter(file)) {
                                    writer.write(stringJson);
                                } catch (Exception e) {
                                    IronsSpellbooks.LOGGER.debug("Failed to generate recipe \"{}\": {}", outputFilepath, e.getMessage());
                                }
                            }
                        }
                );
        return 1;
    }

    private static final String EMPTY_FORMAT =
            """
                    {
                      "conditions": [
                        {
                          "type": "forge:mod_loaded",
                          "modid": "create"
                        }
                      ],
                      "type": "create:emptying",
                      "ingredients": [
                        %s
                      ],
                      "results": [
                        {
                          "item": "%s"
                        },
                        {
                          "fluid": "%s",
                          "amount": %s
                        }
                      ]
                    }
                    """;
    private static final String FILL_FORMAT =
            """
                    {
                      "conditions": [
                        {
                          "type": "forge:mod_loaded",
                          "modid": "create"
                        }
                      ],
                      "type": "create:filling",
                      "ingredients": [
                        %s,
                        {
                          "fluid": "%s",
                          "amount": %s
                        }
                      ],
                      "results": [
                        {
                          "item": "%s"
                        }
                      ]
                    }
                    """;
}
