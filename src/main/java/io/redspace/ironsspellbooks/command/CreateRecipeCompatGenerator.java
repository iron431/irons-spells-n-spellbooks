package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public class CreateRecipeCompatGenerator {

    public static int run(CommandContext<CommandSourceStack> context) {
//        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        new File("create_compat").mkdir();
//        recipeManager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get()).stream()
//                .forEach(
//                        recipeHolder -> {
//                            var recipe = recipeHolder.value();
//                            String stringJson = String.format(FILL_FORMAT,
//                                    Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, recipe.input()).getOrThrow().toString(),
//                                    recipe.fluid().getFluidHolder().getKey().location().toString(),
//                                    recipe.fluid().getAmount(),
//                                    recipe.result().getItemHolder().getKey().location().toString()
//                            );
//                            String outputFilepath = String.format("create_compat/create_fill_%s.json", recipeHolder.id().getPath().split("/", 2)[1].split("_", 2)[1]);
//                            var file = new File(outputFilepath);
//                            try (FileWriter writer = new FileWriter(file)) {
//                                writer.write(stringJson);
//                            } catch (Exception e) {
//                                IronsSpellbooks.LOGGER.debug("Failed to generate recipe \"{}\": {}", outputFilepath, e.getMessage());
//                            }
//                        }
//                );
//        recipeManager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get()).stream()
//                .forEach(
//                        recipeHolder -> {
//                            var recipe = recipeHolder.value();
//                            String stringJson = String.format(EMPTY_FORMAT,
//                                    Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, recipe.input()).getOrThrow().toString(),
//                                    recipe.returned().getItemHolder().getKey().location().toString(),
//                                    recipe.result().getFluidHolder().getKey().location().toString(),
//                                    recipe.result().getAmount()
//                            );
//                            new File("create_compat").mkdir();
//                            String outputFilepath = String.format("create_compat/create_empty_%s.json", recipeHolder.id().getPath().split("/", 2)[1].split("_", 2)[1]);
//                            var file = new File(outputFilepath);
//                            try (FileWriter writer = new FileWriter(file)) {
//                                writer.write(stringJson);
//                            } catch (Exception e) {
//                                IronsSpellbooks.LOGGER.debug("Failed to generate recipe \"{}\": {}", outputFilepath, e.getMessage());
//                            }
//                        }
//                );
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
                          "id": "%s"
                        },
                        {
                          "id": "%s",
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
                          "type": "fluid_stack",
                          "fluid": "%s",
                          "amount": %s
                        }
                      ],
                      "results": [
                        {
                          "id": "%s"
                        }
                      ]
                    }
                    """;
}
