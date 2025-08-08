package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.recipe_types.NoAdditionSmithingTransformRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class RecipeRegistry {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, IronsSpellbooks.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }

    public static final RegistryObject<RecipeSerializer<?>> ALCHEMIST_CAULDRON_FILL_SERIALIZER = RECIPE_SERIALIZERS.register("alchemist_cauldron_fill", FillAlchemistCauldronRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<FillAlchemistCauldronRecipe>> ALCHEMIST_CAULDRON_FILL_TYPE = RECIPE_TYPES.register("alchemist_cauldron_fill",
            // weird syntax, see https://docs.neoforged.net/docs/resources/server/recipes/custom/#the-recipe-type
            registry -> new RecipeType<FillAlchemistCauldronRecipe>() {
                @Override
                public String toString() {
                    return registry.toString();
                }
            });

    public static final RegistryObject<RecipeSerializer<?>> ALCHEMIST_CAULDRON_EMPTY_SERIALIZER = RECIPE_SERIALIZERS.register("alchemist_cauldron_empty", EmptyAlchemistCauldronRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<EmptyAlchemistCauldronRecipe>> ALCHEMIST_CAULDRON_EMPTY_TYPE = RECIPE_TYPES.register("alchemist_cauldron_empty",
            registry -> new RecipeType<EmptyAlchemistCauldronRecipe>() {
                @Override
                public String toString() {
                    return registry.toString();
                }
            });

    public static final RegistryObject<RecipeSerializer<?>> ALCHEMIST_CAULDRON_BREW_SERIALIZER
            = RECIPE_SERIALIZERS.register("alchemist_cauldron_brew", BrewAlchemistCauldronRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<BrewAlchemistCauldronRecipe>> ALCHEMIST_CAULDRON_BREW_TYPE
            = RECIPE_TYPES.register("alchemist_cauldron_brew", registry -> new RecipeType<BrewAlchemistCauldronRecipe>() {
        @Override
        public String toString() {
            return registry.toString();
        }
    });
    public static final RegistryObject<RecipeSerializer<?>> SMITHING_TRANSFORM_NO_ADDITION_SERIALIZER =
            RECIPE_SERIALIZERS.register("smithing_transform_no_addition", NoAdditionSmithingTransformRecipe.Serializer::new);

}
