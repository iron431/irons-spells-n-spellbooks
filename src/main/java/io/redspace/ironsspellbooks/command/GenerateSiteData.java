package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.consumables.SimpleElixir;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateSiteData {

    private static final LazyOptional<List<Item>> ITEM_BLACKLIST = LazyOptional.of(() -> List.of(
            ItemRegistry.INVISIBILITY_RING.get(),
            ItemRegistry.TEST_CLAYMORE.get(),
            ItemRegistry.LURKER_RING.get(),
            ItemRegistry.SPELLBREAKER.get(),
            ItemRegistry.AUTOLOADER_CROSSBOW.get()
    ));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.generate_recipe_data.failed"));

    private static final String RECIPE_DATA_TEMPLATE = """
            - id: "%s"
              name: "%s"
              path: "%s"
              group: "%s"
              craftingType: "%s"
              item0Id: "%s"
              item0: "%s"
              item0Path: "%s"
              item1Id: "%s"
              item1: "%s"
              item1Path: "%s"
              item2Id: "%s"
              item2: "%s"
              item2Path: "%s"
              item3Id: "%s"
              item3: "%s"
              item3Path: "%s"
              item4Id: "%s"
              item4: "%s"
              item4Path: "%s"
              item5Id: "%s"
              item5: "%s"
              item5Path: "%s"
              item6Id: "%s"
              item6: "%s"
              item6Path: "%s"
              item7Id: "%s"
              item7: "%s"
              item7Path: "%s"
              item8Id: "%s"
              item8: "%s"
              item8Path: "%s"
              tooltip: "%s"
              description: ""
              
                    """;

    private static final String SPELL_DATA_TEMPLATE = """
            - name: "%s"
              school: "%s"
              icon: "%s"
              level: "%d to %d"
              mana: "%d to %d"
              cooldown: "%ds"
              cast_type: "%s"
              rarity: "%s to %s"
              description: "%s"
              u1: "%s"
              u2: "%s"
              u3: "%s"
              u4: "%s"
              
                    """;

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("generateSiteData").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).executes((commandContext) -> {
            return generateSiteData(commandContext.getSource());
        }));
    }

    private static int generateSiteData(CommandSourceStack source) {
        generateRecipeData(source);
        generateSpellData();

        return 1;
    }

    private static void generateRecipeData(CommandSourceStack source) {
        try {
            var itemBuilder = new StringBuilder();
            var armorBuilder = new StringBuilder();
            var spellbookBuilder = new StringBuilder();
            var curioBuilder = new StringBuilder();
            var blockBuilder = new StringBuilder();

            Set<Item> itemsTracked = new HashSet<>();
            //This will exclude these items
            itemsTracked.add(ItemRegistry.WIMPY_SPELL_BOOK.get());
            itemsTracked.add(ItemRegistry.LEGENDARY_SPELL_BOOK.get());
            itemsTracked.add(Items.POISONOUS_POTATO);
            handleAffinityRingEntry(curioBuilder, itemsTracked, source);
            ForgeRegistries.ITEMS.getValues()
                    .stream()
                    //.sorted(Comparator.comparing(Item::getDescriptionId))
                    .forEach(item -> {
                        var itemResource = ForgeRegistries.ITEMS.getKey(item);
                        var tooltip = getTooltip(source.getPlayer(), new ItemStack(item));

                        if (itemResource.getNamespace().equals("irons_spellbooks") && !itemsTracked.contains(item) && !ITEM_BLACKLIST.resolve().get().contains(item)) {
                            var recipe = getRecipeFor(source, item);
                            var name = item.getName(ItemStack.EMPTY).getString();
                            if (item.getDescriptionId().contains("patchouli") || item.getDescriptionId().contains("spawn_egg") || item.getDescriptionId().equals("item.irons_spellbooks.scroll")) {
                                //Skip
                            } else if (item instanceof ArmorItem) {
                                if (recipe != null) {
                                    var words = name.split(" ");
                                    var group = Arrays.stream(words).limit(words.length - 1).collect(Collectors.joining(" "));
                                    appendToBuilder(armorBuilder, recipe, getRecipeData(recipe), group, tooltip);
                                } else {
                                    appendToBuilder2(armorBuilder, name, itemResource, tooltip);
                                }
                            } else if (item instanceof CurioBaseItem) {
                                if (recipe != null) {
                                    appendToBuilder(curioBuilder, recipe, getRecipeData(recipe), "", tooltip);
                                } else {
                                    appendToBuilder2(curioBuilder, name, itemResource, tooltip);
                                }
                            } else if (item instanceof UniqueSpellBook) {
                                //should never have recipe
                                appendToBuilder2(spellbookBuilder, name, itemResource, getSpells(new ItemStack(item)));
                            } else if (item instanceof SpellBook || item instanceof ExtendedSwordItem) {
                                if (recipe != null) {
                                    appendToBuilder(spellbookBuilder, recipe, getRecipeData(recipe), "", tooltip);
                                } else {
                                    appendToBuilder2(spellbookBuilder, name, itemResource, tooltip);
                                }
                            } else if (item instanceof BlockItem) {
                                if (recipe != null) {
                                    appendToBuilder(blockBuilder, recipe, getRecipeData(recipe), "", tooltip);
                                } else {
                                    appendToBuilder2(blockBuilder, name, itemResource, tooltip);
                                }
                            } else {
                                if (recipe != null) {
                                    appendToBuilder(itemBuilder, recipe, getRecipeData(recipe), handleGenericItemGrouping(item), tooltip);
                                } else {
                                    appendToBuilder3(itemBuilder, name, itemResource, handleGenericItemGrouping(item), tooltip);
                                }
                            }
                            itemsTracked.add(item);

                        }
                    });

            var file = new BufferedWriter(new FileWriter("item_data.yml"));
            file.write(postProcess(itemBuilder));
            file.close();

            file = new BufferedWriter(new FileWriter("armor_data.yml"));
            file.write(postProcess(armorBuilder));
            file.close();

            file = new BufferedWriter(new FileWriter("curio_data.yml"));
            file.write(postProcess(curioBuilder));
            file.close();

            file = new BufferedWriter(new FileWriter("spellbook_data.yml"));
            file.write(postProcess(spellbookBuilder));
            file.close();

            file = new BufferedWriter(new FileWriter("block_data.yml"));
            file.write(postProcess(blockBuilder));
            file.close();
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.debug(e.getMessage());
        }
    }

    private static void handleAffinityRingEntry(StringBuilder curioBuilder, Set<Item> itemsTracked, CommandSourceStack source) {
        var item = ItemRegistry.AFFINITY_RING.get();
        itemsTracked.add(item);
        var itemResource = ForgeRegistries.ITEMS.getKey(item);
        var name = item.getName(ItemStack.EMPTY).getString();
        appendToBuilder2(curioBuilder, name, itemResource,
                "Affinity Rings are randomly generated as loot, and will boost the level of a select spell by one. This effect can stack."
        );

    }

    private static String handleGenericItemGrouping(Item item) {
        if (item instanceof InkItem) {
            return "Ink";
        } else if (item.getDescriptionId().contains("rune")) {
            return "Runes";
        } else if (item instanceof UpgradeOrbItem || item == ItemRegistry.UPGRADE_ORB.get()) {
            return "Upgrade Orbs";
        } else if (item instanceof SimpleElixir) {
            return "Elixirs";
        } else {
            return "All";
        }
    }

    @NotNull
    private static ArrayList<RecipeIngredientData> getRecipeData(Recipe<?> recipe) {
        var resultItemResourceLocation = ForgeRegistries.ITEMS.getKey(recipe.getResultItem().getItem());
        var recipeData = new ArrayList<RecipeIngredientData>(10);
        recipeData.add(new RecipeIngredientData(
                resultItemResourceLocation.toString(),
                recipe.getResultItem().getItem().getName(ItemStack.EMPTY).getString(),
                String.format("/img/items/%s.png", resultItemResourceLocation.getPath()),
                recipe.getResultItem().getItem())
        );
        if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
            recipe.getIngredients().forEach(ingredient -> {
                handleIngredient(ingredient, recipeData, recipe);
            });
        } else if (recipe instanceof UpgradeRecipe upgradeRecipe) {
            handleIngredient(upgradeRecipe.base, recipeData, recipe);
            handleIngredient(upgradeRecipe.addition, recipeData, recipe);
        }
        return recipeData;
    }

    private static @Nullable Recipe getRecipeFor(CommandSourceStack sourceStack, Item item) {
        for (Recipe recipe : sourceStack.getRecipeManager().getRecipes()) {
            if (recipe.getResultItem().is(item)) {
                return recipe;
            }
        }
        return null;
    }

    private static String postProcess(StringBuilder sb) {
        return sb.toString()
                .replace("netherite_spell_book.png", "netherite_spell_book.gif")
                .replace("ruined_book.png", "ruined_book.gif")
                .replace("lightning_bottle.png", "lightning_bottle.gif")
                .replace("cinder_essence.png", "cinder_essence.gif")
                .replace("nature_upgrade_orb.png", "nature_upgrade_orb.gif")
                .replace("evasion_elixir.png", "evasion_elixir.gif")
                .replace("/upgrade_orb.png", "/upgrade_orb.gif")
                .replace("fire_upgrade_orb.png", "fire_upgrade_orb.gif")
                .replace("holy_upgrade_orb.png", "holy_upgrade_orb.gif")
                .replace("lightning_upgrade_orb.png", "lightning_upgrade_orb.gif")
                .replace("ender_upgrade_orb.png", "ender_upgrade_orb.gif")
                .replace("mana_upgrade_orb.png", "upgrade_orb_mana.gif")
                .replace("protection_upgrade_orb.png", "upgrade_orb_protection.gif")
                .replace("ice_upgrade_orb.png", "upgrade_orb_ice.gif")
                .replace("evocation_upgrade_orb.png", "upgrade_orb_evocation.gif")
                .replace("cooldown_upgrade_orb.png", "upgrade_orb_cooldown.gif")
                .replace("blood_upgrade_orb.png", "upgrade_orb_blood.gif")
                .replace("wayward_compass.png", "wayward_compass.gif");
    }

    private static String getSpells(ItemStack itemStack) {
        if (itemStack.getItem() instanceof SpellBook spellBook) {
            var spellList = spellBook.getSpellList(itemStack);

            return spellList.getActiveSpells().stream().map(spell -> {
                return spell.getSpell().getDisplayName(null).getString() + " (" + spell.getLevel() + ")";
            }).collect(Collectors.joining(", "));
        }
        return "";
    }

    private static String getTooltip(ServerPlayer player, ItemStack itemStack) {
        return Arrays.stream(itemStack.getTooltipLines(player, TooltipFlag.Default.NORMAL)
                        .stream()
                        .skip(1) //First component is always the name. Ignore it
                        .map(Component::getString)
                        .filter(x -> x.trim().length() > 0)
                        .collect(Collectors.joining(", "))
                        .replace(":,", ": ")
                        .replace("  ", " ")
                        .split(","))
                .filter(item -> !item.contains("Slot"))
                .collect(Collectors.joining(","))
                .trim()
                .replace(":", ":<br>");
    }

    private static void appendToBuilder(StringBuilder sb, Recipe recipe, List<RecipeIngredientData> recipeIngredientData, String group, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                getRecipeDataAtIndex(recipeIngredientData, 0).id,
                getRecipeDataAtIndex(recipeIngredientData, 0).name,
                getRecipeDataAtIndex(recipeIngredientData, 0).path,
                group,
                recipe.getType(),
                getRecipeDataAtIndex(recipeIngredientData, 1).id,
                getRecipeDataAtIndex(recipeIngredientData, 1).name,
                getRecipeDataAtIndex(recipeIngredientData, 1).path,
                getRecipeDataAtIndex(recipeIngredientData, 2).id,
                getRecipeDataAtIndex(recipeIngredientData, 2).name,
                getRecipeDataAtIndex(recipeIngredientData, 2).path,
                getRecipeDataAtIndex(recipeIngredientData, 3).id,
                getRecipeDataAtIndex(recipeIngredientData, 3).name,
                getRecipeDataAtIndex(recipeIngredientData, 3).path,
                getRecipeDataAtIndex(recipeIngredientData, 4).id,
                getRecipeDataAtIndex(recipeIngredientData, 4).name,
                getRecipeDataAtIndex(recipeIngredientData, 4).path,
                getRecipeDataAtIndex(recipeIngredientData, 5).id,
                getRecipeDataAtIndex(recipeIngredientData, 5).name,
                getRecipeDataAtIndex(recipeIngredientData, 5).path,
                getRecipeDataAtIndex(recipeIngredientData, 6).id,
                getRecipeDataAtIndex(recipeIngredientData, 6).name,
                getRecipeDataAtIndex(recipeIngredientData, 6).path,
                getRecipeDataAtIndex(recipeIngredientData, 7).id,
                getRecipeDataAtIndex(recipeIngredientData, 7).name,
                getRecipeDataAtIndex(recipeIngredientData, 7).path,
                getRecipeDataAtIndex(recipeIngredientData, 8).id,
                getRecipeDataAtIndex(recipeIngredientData, 8).name,
                getRecipeDataAtIndex(recipeIngredientData, 8).path,
                getRecipeDataAtIndex(recipeIngredientData, 9).id,
                getRecipeDataAtIndex(recipeIngredientData, 9).name,
                getRecipeDataAtIndex(recipeIngredientData, 9).path,
                tooltip
        ));
    }

    private static void appendToBuilder2(StringBuilder sb, String name, ResourceLocation itemResource, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                itemResource.toString(),
                name,
                String.format("/img/items/%s.png", itemResource.getPath()),
                "",
                "none",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", tooltip
        ));
    }

    private static void appendToBuilder3(StringBuilder sb, String name, ResourceLocation itemResource, String group, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                itemResource.toString(),
                name,
                String.format("/img/items/%s.png", itemResource.getPath()),
                group,
                "none",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", tooltip
        ));
    }

    private static void handleIngredient(Ingredient ingredient, ArrayList<RecipeIngredientData> recipeData, Recipe recipe) {
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
                    //Detect a specific ingredient tag and replace it will a gif of all applicable items
                    if (itemStack.is(ModTags.INSCRIBED_RUNES) && recipe.getResultItem().is(ItemRegistry.BLANK_RUNE.get())) {
                        path = "/img/items/all_runes.gif";
                    }
                    recipeData.add(new RecipeIngredientData(
                            itemResource.toString(),
                            itemStack.getItem().getName(ItemStack.EMPTY).getString(),
                            path,
                            recipe.getResultItem().getItem()));

                }, () -> {
                    recipeData.add(RecipeIngredientData.EMPTY);
                });
    }

    private static RecipeIngredientData getRecipeDataAtIndex(List<RecipeIngredientData> recipeIngredientData, int index) {
        if (index < recipeIngredientData.size()) {
            return recipeIngredientData.get(index);
        } else {
            return RecipeIngredientData.EMPTY;
        }
    }

    private record RecipeIngredientData(String id, String name, String path, Item item) {
        public static RecipeIngredientData EMPTY = new RecipeIngredientData("", "", "", null);
    }

    private static void generateSpellData() {
        try {
            var sb = new StringBuilder();

            SpellRegistry.REGISTRY.get().getValues().stream()
                    .filter(st -> (st.isEnabled() && st != SpellRegistry.none()))
                    .forEach(spellType -> {
                        var spellMin = spellType.getMinLevel();
                        var spellMax = spellType.getMaxLevel();

                        var uniqueInfo = spellType.getUniqueInfo(spellMin, null);
                        var u1 = uniqueInfo.size() >= 1 ? uniqueInfo.get(0).getString() : "";
                        var u2 = uniqueInfo.size() >= 2 ? uniqueInfo.get(1).getString() : "";
                        var u3 = uniqueInfo.size() >= 3 ? uniqueInfo.get(2).getString() : "";
                        var u4 = uniqueInfo.size() >= 4 ? uniqueInfo.get(3).getString() : "";

                        sb.append(String.format(SPELL_DATA_TEMPLATE,
                                handleCapitalization(spellType.getSpellName()),
                                handleCapitalization(spellType.getSchoolType().getDisplayName().getString()),
                                String.format("/img/spells/%s.png", spellType.getSpellName()),
                                spellType.getMinLevel(),
                                spellType.getMaxLevel(),
                                spellType.getManaCost(spellMin, null),
                                spellType.getManaCost(spellMax, null),
                                spellType.getSpellCooldown(),
                                handleCapitalization(spellType.getCastType().name()),
                                handleCapitalization(spellType.getRarity(spellMin).name()),
                                handleCapitalization(spellType.getRarity(spellMax).name()),
                                Component.translatable(String.format("%s.guide", spellType.getComponentId())).getString(),
                                u1,
                                u2,
                                u3,
                                u4)
                        );
                    });

            var file = new BufferedWriter(new FileWriter("spell_data.yml"));
            file.write(sb.toString());
            file.close();
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.debug(e.getMessage());
        }
    }

    public static String handleCapitalization(String input) {
        return Arrays.stream(input.toLowerCase().split("[ |_]"))
                .map(word -> {
                    if (word.equals("spell")) {
                        return "";
                    } else {
                        var first = word.substring(0, 1);
                        var rest = word.substring(1);
                        return first.toUpperCase() + rest;
                    }
                })
                .collect(Collectors.joining(" "))
                .trim();
    }

    private enum CraftingType {
        CRAFTING_TABLE,
        SMITHING_TABLE,
        NOT_CRAFTABLE
    }
}
